package com.github.hokkaydo.eplbot.module.code.java;

import com.github.hokkaydo.eplbot.module.code.Runner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class JavaRunner implements Runner {
    private static final String WRAPPER_TEMPLATE = """
        import java.util.*;
        import java.lang.Math;

        public class Wrapper {
            public static void main(String[] args){
                %s
            }
        }""";
    private static final ScheduledExecutorService SCHEDULER = new ScheduledThreadPoolExecutor(1);

    @Override
    public String run(String code, Integer timeout) {
        if (requiresWrapper(code)){
            code = WRAPPER_TEMPLATE.formatted(code);
        }
        StringBuilder builder = new StringBuilder();
        Process process;
        int exitCode;
        ScheduledFuture<?> timer =SCHEDULER.schedule(() -> {
            builder.append("Timeout exceeded. Terminating the process.");
            SCHEDULER.shutdownNow();
            ProcessHandle.current().destroy();
            }, timeout, TimeUnit.SECONDS);

        try {
            process = startProcessInDocker(code);
        } catch (IOException e){
            SCHEDULER.shutdownNow();
            return STR."Server side error with code 10\n\{e.getMessage()}";
        }
        try {
            captureProcessOutput(process,builder); // raises IOException
            exitCode = process.waitFor(); // raise InterruptedException
        } catch (IOException e) {
            return STR."Server side error with code 11\n\{e.getMessage()}";
        } catch (InterruptedException e) {
            return STR."Server side error with code 12\n\{e.getMessage()}";
        } finally {
            SCHEDULER.shutdownNow();
        }
        builder.append("\nExited with code: ").append(exitCode);
        timer.cancel(false);
        SCHEDULER.shutdownNow();
        return builder.toString();
    }
    private Process startProcessInDocker(String code) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder(
                "docker", "run", "--rm", "-v", "$(pwd)/logs:/usr/src/app/logs", "java-runner", code);
        processBuilder.redirectErrorStream(true);
        return processBuilder.start();
    }
    private boolean captureProcessOutput(Process process, StringBuilder builder) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append("\n");
            }
            return true;
        }
    }
    public static boolean requiresWrapper(String javaCode) {
        boolean hasClass = Pattern.compile("\\bpublic\\s+class\\s+[A-Z][a-zA-Z0-9]*").matcher(javaCode).find();
        boolean hasMainMethod = Pattern.compile("\\bpublic\\s+static\\s+void\\s+main\\s*\\(\\s*String\\[]\\s+[a-zA-Z0-9]*\\s*\\)").matcher(javaCode).find();
        return !hasMainMethod && !hasClass;
    }

}
