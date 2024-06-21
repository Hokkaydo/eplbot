package com.github.hokkaydo.eplbot.module.code.command;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class TextToFile {
    private static final String TEMP_DIR = STR."\{System.getProperty("user.dir")}\{File.separator}\{File.separator}temp";
    public File createTempFile(String name, String content){
        try (FileWriter myWriter = new FileWriter((STR."%s\{File.separator}\{name}").formatted(TEMP_DIR))){
            myWriter.write(content);
            return new File((STR."%s\{File.separator}\{name}").formatted(TEMP_DIR));
        } catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }
}
