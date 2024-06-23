package com.github.hokkaydo.eplbot.module.code;


import net.dv8tion.jda.internal.utils.tuple.Pair;

public interface Runner {

    Pair<String, Integer> run(String code, Integer timeout);
}
