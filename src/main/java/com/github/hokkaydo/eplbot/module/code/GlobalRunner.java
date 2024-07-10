package com.github.hokkaydo.eplbot.module.code;

import com.github.hokkaydo.eplbot.Main;
import com.github.hokkaydo.eplbot.Strings;
import net.dv8tion.jda.internal.utils.tuple.Pair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.regex.Pattern;

public class GlobalRunner implements Runner{
    private final String targetDocker;
    private final String dockerName;
    public GlobalRunner(String targetDocker, String dockerId){
        this.targetDocker = targetDocker;
        this.dockerName = targetDocker+'-' +dockerId;
    }
    private static final ScheduledExecutorService SCHEDULER = new ScheduledThreadPoolExecutor(1);

    @Override
    public Pair<String, Integer> run(String code, Integer timeout) {
        if (safeMentions(code)){
            return Pair.of(Strings.getString("COMMAND_CODE_UNSAFE_MENTIONS_SUBMITTED"),0);
        }
        StringBuilder builder = new StringBuilder();
        AtomicReference<Process> processRef = new AtomicReference<>();
        int exitCode;

        ScheduledFuture<?> timer =SCHEDULER.schedule(() -> {
            builder.append("Timeout exceeded. Terminating the process.");
            Process process = processRef.get();
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
            }
            deleteDocker();
            }, timeout, TimeUnit.SECONDS);
        Process process;
        try {
            process = startProcessInDocker(code);
            processRef.set(process);
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
        if (safeMentions(builder.toString())){
            return Pair.of(Strings.getString("COMMAND_CODE_UNSAFE_MENTIONS_RESPONSE"),0);
        }
        return Pair.of(builder.toString(),0);
    }

    private Process startProcessInDocker(String code) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder(
                "docker", "run", "--rm", "-v", "/tmp/logs:/usr/src/app/logs","--name", dockerName, targetDocker, code);
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
    public static boolean safeMentions(String result){
        return result.contains("@everyone") || result.contains("@here") || Pattern.compile("<@&?\\d+>").matcher(result).find(); // <@&__ID__> corresponds to a discord role
    }
    public void deleteDocker(){
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("docker", "rm","--force", dockerName);
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                Main.LOGGER.log(Level.WARNING,STR."Couldn't delete the docker with name : \{dockerName}");
            }
        } catch (IOException | InterruptedException e) {
            Main.LOGGER.log(Level.WARNING,STR."Exception when trying to delete the docker : \{dockerName}\n\{e}");
        }
    }
}
