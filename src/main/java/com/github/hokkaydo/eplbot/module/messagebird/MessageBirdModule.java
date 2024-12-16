package com.github.hokkaydo.eplbot.module.messagebird;

import com.github.hokkaydo.eplbot.command.Command;
import com.github.hokkaydo.eplbot.module.Module;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageBirdModule extends Module {

    private final MessageBirdNextMessageCommand messageBirdNextMessageCommand;
    private final ManageMessageBirdCommand manageMessageBirdCommand;
    private final List<ListenerAdapter> registeredListeners = new java.util.ArrayList<>();

    public MessageBirdModule(@NotNull Long guildId) {
        super(guildId);
        Map<String, MessageBirdListener> listeners = new HashMap<>();
        listeners.put("EARLY", new MessageBirdListener(guildId, "EARLY"));
        listeners.put("NIGHT", new MessageBirdListener(guildId, "NIGHT"));
        this.messageBirdNextMessageCommand = new MessageBirdNextMessageCommand(guildId, List.copyOf(listeners.keySet()));
        this.manageMessageBirdCommand = new ManageMessageBirdCommand(listeners);
        this.registeredListeners.addAll(listeners.values());
        this.registeredListeners.add(messageBirdNextMessageCommand);
    }

    @Override
    public String getName() {
        return "messagebird";
    }

    @Override
    public List<Command> getCommands() {
        return List.of(messageBirdNextMessageCommand, manageMessageBirdCommand);
    }

    @Override
    public List<ListenerAdapter> getListeners() {
        return registeredListeners;
    }

}
