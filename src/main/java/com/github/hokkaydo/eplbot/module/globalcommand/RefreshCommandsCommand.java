package com.github.hokkaydo.eplbot.module.globalcommand;

import com.github.hokkaydo.eplbot.Main;
import com.github.hokkaydo.eplbot.Strings;
import com.github.hokkaydo.eplbot.command.Command;
import com.github.hokkaydo.eplbot.command.CommandContext;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class RefreshCommandsCommand implements Command {

    @Override
    public void executeCommand(CommandContext context) {
        Main.getCommandManager().refreshCommands(context.author().getGuild());
        context.replyCallbackAction().setContent(Strings.getString("command.refresh.success")).queue();
    }

    @Override
    public String getName() {
        return "refreshcommands";
    }

    @Override
    public Supplier<String> getDescription() {
        return () -> Strings.getString("command.refresh.description");
    }

    @NotNull
    @Override
    public List<OptionData> getOptions() {
        return Collections.emptyList();
    }

    @Override
    public boolean ephemeralReply() {
        return false;
    }

    @Override
    public boolean validateChannel(MessageChannel channel) {
        return !(channel instanceof PrivateChannel);
    }

    @Override
    public boolean adminOnly() {
        return true;
    }

    @Override
    public Supplier<String> help() {
        return () -> Strings.getString("command.refresh.help");
    }

}
