package com.github.hokkaydo.eplbot.module.messagebird;

import com.github.hokkaydo.eplbot.Strings;
import com.github.hokkaydo.eplbot.command.Command;
import com.github.hokkaydo.eplbot.command.CommandContext;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class ManageMessageBirdCommand implements Command {

    private final Map<String, MessageBirdTask> tasks;

    ManageMessageBirdCommand(Map<String, MessageBirdTask> tasks) {
        this.tasks = tasks;
    }

    @Override
    public void executeCommand(CommandContext context) {
        String subcommand = context.getOption("subcommand").map(OptionMapping::getAsString).orElse(null);
        String type = context.getOption("type").map(OptionMapping::getAsString).orElse(null);

        if (subcommand == null || type == null) {
            context.replyCallbackAction().setContent("command.message_bird.invalid_subcommand").queue();
            return;
        }
        MessageBirdTask l = tasks.get(type);
        switch (subcommand) {
            case "start" -> l.restart();
            case "stop" -> l.stop();
            case "reload_messages" -> l.reloadMessages();
            default -> {
                context.replyCallbackAction().setContent(Strings.getString("command.message_bird.invalid_subcommand")).queue();
                return;
            }
        }
        context.replyCallbackAction().setContent("Done !").queue();
    }

    @Override
    public String getName() {
        return "messagebirdmanage";
    }

    @Override
    public Supplier<String> getDescription() {
        return () -> Strings.getString("command.message_bird.manage.description");
    }

    @NotNull
    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.STRING, "subcommand", "Subcommand", true)
                        .addChoice("restart", "restart")
                        .addChoice("stop", "stop")
                        .addChoice("reload_messages", "reload_messages"),
                new OptionData(OptionType.STRING, "type", "Type", true)
                        .addChoices(this.tasks.keySet().stream().map(s -> new net.dv8tion.jda.api.interactions.commands.Command.Choice(s, s)).toList())
        );
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
        return true;
    }

    @Override
    public Supplier<String> help() {
        return () -> Strings.getString("command.message_bird.manage.help");
    }

}
