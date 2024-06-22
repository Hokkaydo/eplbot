package com.github.hokkaydo.eplbot.module.code.java;

import com.github.hokkaydo.eplbot.Strings;
import com.github.hokkaydo.eplbot.module.code.Runner;
import net.dv8tion.jda.internal.utils.tuple.Pair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import static java.lang.StringTemplate.STR;

public class JavaRunner implements Runner {
    private static final String WRAPPER_TEMPLATE = """
        import java.util.*;
        import java.lang.Math;

        public class Main {
            public static void main(String[] args){
                %s
            }
        }""";
    private static final ScheduledExecutorService SCHEDULER = new ScheduledThreadPoolExecutor(1);

    @Override
    public Pair<String,Integer> run(String code, Integer timeout) {
        if (requiresWrapper(code)){
            code = WRAPPER_TEMPLATE.formatted(code);
        }
        if (!containsMainClass(code)){
            return Pair.of(Strings.getString("COMMAND_CODE_NO_MAIN_CLASS_FOUND"),1);
        }
        if (!hasMainMethod(code)){
            return Pair.of(Strings.getString("COMMAND_CODE_NO_MAIN_CLASS_FOUND"),1);
        }
        StringBuilder builder = new StringBuilder();
        Process process;
        int exitCode;
        ScheduledFuture<?> timer =SCHEDULER.schedule(() -> {
            builder.append("Timeout exceeded. Terminating the process.");
            ProcessHandle.current().destroy();
            }, timeout, TimeUnit.SECONDS);

        try {
            process = startProcessInDocker(code);
        } catch (IOException e){
            return Pair.of(STR."Server side error with code 10\n\{e.getMessage()}", 1);
        }
        try {
            captureProcessOutput(process,builder); // raises IOException
            exitCode = process.waitFor(); // raise InterruptedException
        } catch (IOException e) {
            return Pair.of(STR."Server side error with code 11\n\{e.getMessage()}",1);
        } catch (InterruptedException e) {
            return Pair.of(STR."Server side error with code 12\n\{e.getMessage()}",1);
        }
        builder.append("\nExited with code: ").append(exitCode);
        timer.cancel(false);
        return Pair.of(builder.toString(),0);
    }
    private Process startProcessInDocker(String code) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder(
                "docker", "run", "--rm", "-v", "/tmp/logs:/usr/src/app/logs", "java-runner", code);
        processBuilder.redirectErrorStream(true);
        return processBuilder.start();
    }
    private void captureProcessOutput(Process process, StringBuilder builder) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append("\n");
            }
        }
    }
    private static boolean requiresWrapper(String code) {
        boolean hasClass = Pattern.compile("\\bpublic\\s+class\\s+[A-Z][a-zA-Z0-9]*").matcher(code).find();
        return !hasClass;
    }

    public static boolean containsMainClass(String code) {
        return Pattern.compile("(?s)\\bpublic\\s+class\\s+Main\\b.*?\\{.*?\\bpublic\\s+static\\s+void\\s+main\\s*\\(\\s*String\\s*\\[\\s*]\\s*\\w*\\s*\\)\\s*\\{.*?}.*?}").matcher(code).find();
    }
    public static boolean hasMainMethod(String code){
        return Pattern.compile("\\bpublic\\s+static\\s+void\\s+main\\s*\\(\\s*String\\[]\\s+[a-zA-Z0-9]*\\s*\\)").matcher(code).find();
    }

}
