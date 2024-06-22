package com.github.hokkaydo.eplbot.module.code;

import com.github.hokkaydo.eplbot.command.Command;
import com.github.hokkaydo.eplbot.module.Module;
import com.github.hokkaydo.eplbot.module.code.command.CodeCommand;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class CodeModule extends Module {
    private final CodeCommand codeCommand;
    public CodeModule(@NotNull Long guildId) {
        super(guildId);
        codeCommand = new CodeCommand();
    }

    @Override
    public String getName() {
        return "code";
    }

    @Override
    public List<Command> getCommands() {
        return List.of(codeCommand);
    }

    @Override
    public List<ListenerAdapter> getListeners() {
        return Collections.singletonList(codeCommand);
    }
}
