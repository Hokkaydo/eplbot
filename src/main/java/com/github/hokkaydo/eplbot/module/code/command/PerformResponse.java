package com.github.hokkaydo.eplbot.module.code.command;

import com.github.hokkaydo.eplbot.Main;
import com.github.hokkaydo.eplbot.Strings;
import com.github.hokkaydo.eplbot.module.code.GlobalRunner;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.utils.FileUpload;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;

/**
 *
 */
public class PerformResponse {
    private static final String OUT_FILE = "result.txt";
    private static final String OUT_RESPONSE = "code.txt";
    private static final long MAX_SENT_FILE_SIZE = 8L * 1024 * 1024;
    private boolean validateMessageLength(String content){
        return content.length() < 1960; //count ``` and language name
    }

    /**
     * Sends the serverFile in the textChannel
     * @param textChannel the channel of the interaction
     * @param serverFile the file to be sent trough discord
     * @param filename the name of the serverFile
     */
    private void sendResponseAsFile(MessageChannel textChannel, File serverFile, String filename) {
        FileUpload file = FileUpload.fromData(serverFile, filename);
        textChannel.sendFiles(file)
                .queue( _ -> deleteFile(serverFile));
    }
    /**
     * @param file the file to be sent
     * @return true if the file is longer than 8mb
     */
    private boolean validateFileSize(File file){
        long fileSizeInBytes = file.length();
        return fileSizeInBytes <= MAX_SENT_FILE_SIZE;
    }

    /**
     * @param textChannel the channel of the interaction
     * @param input a string with the data to be written in the file
     * @param fileName the name of the file
     * @return a File
     */
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

    /**
     * sends the data to the textChannel
     * @param textChannel the channel of the interaction
     * @param data any String
     */
    public void sendMessageInChannel(MessageChannel textChannel, String data){
        textChannel.sendMessage(data).queue();
    }

    /**
     * @param textChannel the channel of the interaction
     * @param code the code that has been submitted
     * @param lang
     */
    public void sendSubmittedCode(MessageChannel textChannel, String code, String lang){
        if (GlobalRunner.safeMentions(code)){
            textChannel.sendMessage(STR."\{Strings.getString("COMMAND_CODE_UNSAFE_MENTIONS_SUBMITTED")}\n").queue();
            return;
        }
        if (validateMessageLength(code)){
            textChannel.sendMessage(STR."```\{lang.toLowerCase()}\n\{code}\n```").queue();
            return;
        }
        File responseFile = createFileFromString(textChannel,code,OUT_RESPONSE);
        sendResponseAsFile(textChannel, responseFile,OUT_RESPONSE);
        deleteFile(responseFile);

    }

    /**
     * @param textChannel the channel of the interaction
     * @param result the string with the output of the code
     * @param exitCode an int with the exit code of the submitted code
     */
    @SuppressWarnings("unused") //exitCode is not used but could be later
    public void sendResult(MessageChannel textChannel, String result, int exitCode){
        if (validateMessageLength(result)){
            textChannel.sendMessage(STR."`\{result}`").queue();
            return;
        }
        File responseFile = createFileFromString(textChannel,result,OUT_FILE);
        if (responseFile != null && validateFileSize(responseFile)){
            sendResponseAsFile(textChannel,responseFile,OUT_FILE);
            deleteFile(responseFile);
            return;
        }
        deleteFile(responseFile);
        sendMessageInChannel(textChannel, Strings.getString("COMMAND_CODE_EXCEEDED_FILE_SIZE"));

    }

    /**
     * @param file the file to be deleted
     */
    public void deleteFile(File file){
        if (file!= null && !file.delete()){
            Main.LOGGER.log(Level.INFO,"File not deleted");
        }
    }
}
