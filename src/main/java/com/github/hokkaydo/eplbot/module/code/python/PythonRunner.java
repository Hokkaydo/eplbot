package com.github.hokkaydo.eplbot.module.code.python;

import com.github.hokkaydo.eplbot.module.code.Runner;
import net.dv8tion.jda.internal.utils.tuple.Pair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class PythonRunner implements Runner {
    private static final ScheduledExecutorService SCHEDULER = new ScheduledThreadPoolExecutor(1);
    @Override
    public Pair<String, Integer> run(String code, Integer timeout) {
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
                "docker", "run", "--rm", "-v", "/tmp/logs:/usr/src/app/logs", "python-runner", code);
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
}
