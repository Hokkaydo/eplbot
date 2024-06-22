package com.github.hokkaydo.eplbot.module.code.python;

import com.github.hokkaydo.eplbot.module.code.GlobalRunner;
import com.github.hokkaydo.eplbot.module.code.Runner;
import net.dv8tion.jda.internal.utils.tuple.Pair;


public class PythonRunner implements Runner {
    GlobalRunner runner = new GlobalRunner("python-runner");

    @Override
    public Pair<String, Integer> run(String code, Integer timeout) {
        return this.runner.run(code, timeout);
    }
}
