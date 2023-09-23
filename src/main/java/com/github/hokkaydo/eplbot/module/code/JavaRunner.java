package com.github.hokkaydo.eplbot.module.code;
import java.io.File;
import java.io.IOException;
import javax.tools.ToolProvider;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.io.InputStream;
public class JavaRunner {
    private static final String OUTPUT_PATH = System.getProperty("user.dir")+"\\src\\temp\\";
    private static final String JAVA_PATH = "temp";
    public static String run(String input) {
        String safe = safeImports(input);
        if (!input.equals(safe)){return "Unvalid imports";};
        String class_name = regexClassName(input);
        writeFile(input,Path.of(OUTPUT_PATH+"\\"+class_name+".java"));
        String filePath = OUTPUT_PATH + "\\" + class_name + ".java";
        try {
            ByteArrayOutputStream error_stream = new ByteArrayOutputStream();
            if (ToolProvider.getSystemJavaCompiler().run(null, null, new PrintStream(error_stream), filePath) == 0) {
                try {
                    Process process = new ProcessBuilder(System.getProperty("java.home") + File.separator + "bin" + File.separator + "java", "-cp", System.getProperty("java.class.path") + File.pathSeparator + OUTPUT_PATH,regexClassName(input) ).redirectErrorStream(true).start();
                    InputStream inputStream = process.getInputStream();
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    if (process.waitFor() == 0) {
                        for (File file : new File(OUTPUT_PATH).listFiles()) {
                            if (file.isFile() && file.getName().startsWith(class_name)) {
                                file.delete();
                            }                            
                        }
                    return outputStream.toString();
                    } else {
                        return "Run failed:\n" + outputStream.toString();
                    }
                } catch (IOException | InterruptedException e) {
                    return "Run failed:\n" + e.toString();
                }
            } else {

                return "Compilation failed:\n" + error_stream.toString();
            }
    
        } catch (Exception e) {
            e.printStackTrace();
            return e.toString();
        }
    }

    public static void writeFile(String input, Path path) {
        try {
            Files.createDirectories(path.getParent()); // Create parent directories if they don't exist
            Files.write(path, input.getBytes(), StandardOpenOption.CREATE);
            System.out.println("File written: " + path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static String packageBuilder(String input){
        return "package "+JAVA_PATH+";\n" + input;

    }
    public static String regexDetector(String input){
        Matcher matcher = Pattern.compile("import\\s+[^;]+;", Pattern.MULTILINE).matcher(input);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String importStatement = matcher.group(0);
            result.append(importStatement).append("\n");
        }
        return result.toString();
    }
    public static String regexClassName(String input){
        Matcher matcher = Pattern.compile("class\\s+(\\w+)\\s*\\{").matcher(input);
        if (matcher.find()) {
            return matcher.group(1).replaceAll("\\s+", "");
        } else {
            throw new RuntimeException("No class definition found.");}

    }
    public static String safeImports(String input){
        List<String> dangerousImports = Arrays.asList(
            "java.lang.reflect",
            "java.io",
            "java.util.zip",
            "java.net",
            "java.nio.file",
            "java.security",
            "java.awt",
            "javax.swing",
            "javax.script",
            "java.util.logging",
            "java.lang.ProcessBuilder",
            "java.lang.Runtime",
            "com",
            "net",
            "org"
        );
        String regex = "(?i)import\\s+" + String.join("|", dangerousImports).replaceAll("\\.", "\\\\.") + "\\s*;";
        return input.replaceAll(regex, "");
    }

}