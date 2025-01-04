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

    public MessageBirdModule(@NotNull Long guildId) {
        super(guildId);
        Map<String, MessageBirdTask> tasks = new HashMap<>();
        tasks.put("EARLY", new MessageBirdTask(guildId, "EARLY"));
        tasks.put("NIGHT", new MessageBirdTask(guildId, "NIGHT"));
        this.messageBirdNextMessageCommand = new MessageBirdNextMessageCommand(guildId, List.copyOf(tasks.keySet()));
        this.manageMessageBirdCommand = new ManageMessageBirdCommand(tasks);
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
        return List.of(messageBirdNextMessageCommand);
    }

}
