package com.github.hokkaydo.eplbot.module.globalcommand;

import com.github.hokkaydo.eplbot.Main;
import com.github.hokkaydo.eplbot.Strings;
import com.github.hokkaydo.eplbot.command.Command;
import com.github.hokkaydo.eplbot.command.CommandContext;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.internal.utils.Helpers;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class SayCommand implements Command {

    private final List<SayRecord> records  = new ArrayList<>();

    @Override
    public void executeCommand(CommandContext context) {
        String message = context.getOption("message").map(OptionMapping::getAsString).orElse("");
        Channel channel = context.getOption("channel").map(OptionMapping::getAsChannel).orElse(null);
        String replyTo = context.getOption("reply-to").map(OptionMapping::getAsString).orElse("");

        if(context.user().getIdLong() == Main.getBossId() && (message.equals("_logs") || message.equals("_records") || message.equals("_log") || message.equals("_record"))) {
            context.replyCallbackAction()
                    .setEphemeral(true)
                    .setContent(records.isEmpty() ?
                                        "[]" :
                                        records.stream()
                                                .map(r -> String.format("jump: %s, author: %s%n", r.url, r.author))
                                                .collect(Collectors.joining())
                    )
                    .queue();
            return;
        }

        if (context.interaction().getGuild() == null) {
            context.replyCallbackAction().setEphemeral(true).setContent(Strings.getString("SAY_COMMAND_GUILD_ONLY")).queue();
            return;
        }
        channel = channel == null ? context.channel() : channel;

        if (!(channel instanceof MessageChannel textChannel)) {
            context.replyCallbackAction().setEphemeral(true).setContent(Strings.getString("SAY_COMMAND_NOT_TEXT_CHANNEL")).queue();
            return;
        }

        if (!replyTo.isBlank()) {
            if (replyTo.length() > 20 || !Helpers.isNumeric(replyTo)) {
                context.replyCallbackAction().setEphemeral(true).setContent(Strings.getString("SAY_COMMAND_MESSAGE_NOT_FOUND")).queue();
                return;
            }
            textChannel.retrieveMessageById(replyTo).queue(msg -> msg.reply(message).queue(res -> {
                records.add(new SayRecord(res.getJumpUrl(), context.user().getName(), System.currentTimeMillis()));
                context.replyCallbackAction().setEphemeral(true).setContent(Strings.getString("SAY_COMMAND_SUCCESS")).queue();
            }), ignored -> context.replyCallbackAction().setEphemeral(true).setContent(Strings.getString("SAY_COMMAND_MESSAGE_NOT_FOUND")).queue());
        } else {
            textChannel.sendMessage(message).queue(res -> {
                records.add(new SayRecord(res.getJumpUrl(), context.user().getName(), System.currentTimeMillis()));
                context.replyCallbackAction().setEphemeral(true).setContent(Strings.getString("SAY_COMMAND_SUCCESS")).queue();
            });
        }
    }

    public void periodicCleanup() {
        try (ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor()) {
            executor.scheduleAtFixedRate(() -> {
                long now = System.currentTimeMillis();
                records.removeIf(r -> now - r.timestamp > TimeUnit.DAYS.toMillis(30));
            }, 0, 30, TimeUnit.DAYS);
        }
    }

    @Override
    public String getName() {
        return "say";
    }

    @Override
    public Supplier<String> getDescription() {
        return () -> Strings.getString("SAY_COMMAND_DESCRIPTION");
    }

    @NotNull
    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.STRING, "message", Strings.getString("SAY_COMMAND_OPTION_MESSAGE_DESCRIPTION"), true),
                new OptionData(OptionType.CHANNEL, "channel", Strings.getString("SAY_COMMAND_OPTION_CHANNEL_DESCRIPTION"), false),
                new OptionData(OptionType.STRING, "reply-to", Strings.getString("SAY_COMMAND_OPTION_REPLY_TO_DESCRIPTION"), false)
        );
    }

    @Override
    public boolean ephemeralReply() {
        return true;
    }

    @Override
    public boolean validateChannel(MessageChannel channel) {
        return channel instanceof GuildChannel;
    }

    @Override
    public boolean adminOnly() {
        return true;
    }

    @Override
    public Supplier<String> help() {
        return () -> Strings.getString("SAY_COMMAND_HELP");
    }

    private record SayRecord(String url, String author, long timestamp) {}

}
