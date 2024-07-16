package com.github.hokkaydo.eplbot.module.code.python;

import com.github.hokkaydo.eplbot.module.code.GlobalRunner;
import com.github.hokkaydo.eplbot.module.code.Runner;
import net.dv8tion.jda.internal.utils.tuple.Pair;

public class PythonRunner implements Runner {
    String processId;
    GlobalRunner runner;
    public PythonRunner(String processId){
        this.processId = processId;
        this.runner = new GlobalRunner("python-runner",processId); // python-runner is linked to /build_code_docker.sh
    }
    @Override
    public Pair<String, Integer> run(String code, Integer timeout) {
        return this.runner.run(parseBackslashChars(code), timeout);
    }

    /**
     * Simple function to parse the \, as the given code trough discord contains \n maybe inside the strings
     * that needs to be seen as \\n to be written inside the docker
     * @param code the code to parse
     * @return a string where "\n" becomes "\\n"
     */
    public static String parseBackslashChars(String code){
        StringBuilder result = new StringBuilder();
        boolean isInString = false;
        for (int i = 0; i < code.length(); i++){
            Character c = code.charAt(i);
            if (c == '\'' || c == '\"'){ // checks for ..."string\n" or 'string\n'
                isInString = !isInString;
            }
            if (isInString && c == '\\'){
                result.append("\\\\");
                continue;
            }
            result.append(c);
        }
        return result.toString();
    }
}
