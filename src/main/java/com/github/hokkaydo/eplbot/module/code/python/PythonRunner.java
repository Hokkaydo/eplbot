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
        if (code.isEmpty()){  //checks to avoid IndexOutOfBoundException being thrown  when creating the isInString bool
            return code;
        }
        StringBuilder result = new StringBuilder();
        result.append(code.charAt(0));
        boolean isInString = (code.charAt(0) == '\'' || code.charAt(0) == '\"');
        // we start at index 1 because the code sent might begin with " which would be ignored to avoid an error
        // when checking for charAt(i-1), see commit 7bd90cea92d5f5a54e8af2529b59c519a7b7662a for a more
        // concise implementation that could break with a code starting with "
        for (int i = 1; i < code.length(); i++){
            Character c = code.charAt(i);
            if ((c == '\'' || c == '\"') && (code.charAt(i-1) != '\\')){ // checks for "..." or '...' but not "\""
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
