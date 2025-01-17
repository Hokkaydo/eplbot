package com.github.hokkaydo.eplbot.module.confession;

import com.github.hokkaydo.eplbot.Strings;
import com.github.hokkaydo.eplbot.command.Command;
import com.github.hokkaydo.eplbot.command.CommandContext;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

public class ConfessionCommand extends ListenerAdapter implements Command {

    private final ConfessionProcessor confessionProcessor;
    ConfessionCommand(ConfessionProcessor processor) {
        this.confessionProcessor = processor;
    }

    @Override
    public void executeCommand(CommandContext context) {
        confessionProcessor.process(context, false);
    }

    @Override
    public String getName() {
        return "confess";
    }

    @Override
    public Supplier<String> getDescription() {
        return () -> Strings.getString("command.confession.description");
    }

    @NotNull
    @Override
    public List<OptionData> getOptions() {
        return List.of();
    }

    @Override
    public boolean ephemeralReply() {
        return true;
    }

    @Override
    public boolean validateChannel(MessageChannel channel) {
        return true;
    }

    @Override
    public boolean adminOnly() {
        return false;
    }

    @Override
    public Supplier<String> help() {
        return () -> Strings.getString("command.confession.help");
    }

}
