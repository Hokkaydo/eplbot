package com.github.hokkaydo.eplbot.module.code.command;

import com.github.hokkaydo.eplbot.Main;
import com.github.hokkaydo.eplbot.Strings;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.utils.FileUpload;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;

public class PerformResponse {
    private static final String OUT_FILE = "result.txt";
    private static final String OUT_RESPONSE = "code.txt";
    private static final long MAX_SENT_FILE_SIZE = 8L * 1024 * 1024;
    private boolean validateMessageLength(String content){
        return content.length() < 1960; //count ``` and language name
    }
    private void sendResponseAsFile(MessageChannel textChannel, File serverFile, String filename) {
        FileUpload file = FileUpload.fromData(serverFile, filename);
        textChannel.sendFiles(file)
                .queue(s -> serverFile.delete());
    }
    private boolean validateFileSize(File file){
        long fileSizeInBytes = file.length();
        return fileSizeInBytes <= MAX_SENT_FILE_SIZE;
    }


    private File createFileFromString(MessageChannel textChannel, String input, String fileName){
        try (FileWriter myWriter = new FileWriter((STR."\{fileName}"))){
            myWriter.write(input);
            return new File((STR."\{fileName}"));
        } catch (IOException e){
            Main.LOGGER.log(Level.INFO,"Couldn't create a temporary virtual file when trying to respond to submitted code");
            sendMessageInChannel(textChannel,STR."Server side error with code 00\n\{e.getMessage()}");
            return null;
        }
    }
    public void sendMessageInChannel(MessageChannel textChannel, String data){
        textChannel.sendMessage(data).queue();
    }
    public void sendSubmittedCode(MessageChannel textChannel, String code, String lang){
        if (validateMessageLength(code)){
            textChannel.sendMessage(STR."```\{lang.toLowerCase()}\n\{code}\n```").queue();
            return;
        }
        File responseFile = createFileFromString(textChannel,code,OUT_RESPONSE);
        sendResponseAsFile(textChannel, responseFile,OUT_RESPONSE);
        responseFile.delete();

    }
    public void sendResult(MessageChannel textChannel, String result, int exitCode){
        if (validateMessageLength(result)){
            textChannel.sendMessage(STR."`Exit code : \{exitCode}\n\{result}`").queue();
            return;
        }
        File responseFile = createFileFromString(textChannel,result,OUT_FILE);
        if (validateFileSize(responseFile)){
            sendResponseAsFile(textChannel,responseFile,OUT_FILE);
            responseFile.delete();
            return;
        }
        responseFile.delete();
        sendMessageInChannel(textChannel, Strings.getString("COMMAND_CODE_EXCEEDED_FILE_SIZE"));

    }
}
