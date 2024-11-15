package com.github.hokkaydo.eplbot.module.menu;

import com.github.hokkaydo.eplbot.command.Command;
import com.github.hokkaydo.eplbot.module.Module;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MenuModule extends Module {

    private final MenuSender menuSender;
    private final MenuCommand menuCommand;

    public MenuModule(@NotNull Long guildId) {
        super(guildId);
        menuSender = new MenuSender(guildId);
        menuCommand = new MenuCommand(menuSender);
        menuSender.start();
    }

    @Override
    public String getName() {
        return "menu";
    }

    @Override
    public List<Command> getCommands() {
        return List.of(menuCommand);
    }

    @Override
    public List<ListenerAdapter> getListeners() {
        return List.of();
    }

    @Override
    public void disable() {
        super.disable();
        menuSender.stop();
    }

}
