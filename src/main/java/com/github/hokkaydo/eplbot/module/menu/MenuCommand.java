package com.github.hokkaydo.eplbot.module.menu;

import com.github.hokkaydo.eplbot.Strings;
import com.github.hokkaydo.eplbot.command.Command;
import com.github.hokkaydo.eplbot.command.CommandContext;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class MenuCommand implements Command {

    private final MenuRetriever menuRetriever;

    MenuCommand(MenuRetriever menuRetriever) {
        this.menuRetriever = menuRetriever;
    }

    @Override
    public void executeCommand(CommandContext context) {
        menuRetriever.retrieveMenu().ifPresentOrElse(
                menu -> context.replyCallbackAction().setContent(menu).queue(),
                () -> context.replyCallbackAction().setContent(Strings.getString("MENU_COMMAND_NOT_FOUND")).queue()
        );
    }

    @Override
    public String getName() {
        return "menu";
    }

    @Override
    public Supplier<String> getDescription() {
        return () -> Strings.getString("MENU_COMMAND_DESCRIPTION");
    }

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
        return true;
    }

    @Override
    public boolean adminOnly() {
        return false;
    }

    @Override
    public Supplier<String> help() {
        return () -> Strings.getString("MENU_COMMAND_HELP");
    }

}
