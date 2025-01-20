package com.github.hokkaydo.eplbot.module.globalcommand;

import com.github.hokkaydo.eplbot.Strings;
import com.github.hokkaydo.eplbot.command.Command;
import com.github.hokkaydo.eplbot.command.CommandContext;
import com.github.hokkaydo.eplbot.configuration.Config;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class ConfigurationCommand implements Command {

    private final Long guildId;
    ConfigurationCommand(Long guildId) {
        this.guildId = guildId;
    }
    @Override
    public void executeCommand(CommandContext context) {
        Optional<OptionMapping> keyOption = context.options().stream().filter(s -> s.getName().equals("key")).findFirst();
        Optional<OptionMapping> valueOption = context.options().stream().filter(s -> s.getName().equals("value")).findFirst();
        if(keyOption.isEmpty()) {
            context.replyCallbackAction().setContent(
                    Config.getDefaultConfiguration()
                            .keySet()
                            .stream()
                            .sorted()
                            .map(k -> "`%s`: %s".formatted(k, Config.getGuildVariable(guildId, k)))
                            .reduce("%s%n%s"::formatted)
                            .orElse("")
            ).queue();
            return;
        }
        if(valueOption.isEmpty()) {
            context.replyCallbackAction().setContent(
                    Config.getDefaultConfiguration().keySet().stream()
                            .filter(k -> k.equals(keyOption.get().getAsString()))
                            .map(k -> "`%s`: %s".formatted(k, Config.getGuildVariable(guildId, k)))
                            .findFirst()
                            .orElse("")
            ).queue();
            return;
        }
        boolean success = Config.parseAndUpdate(guildId, keyOption.get().getAsString(), valueOption.get().getAsString());
        if(success) {
            context.replyCallbackAction().setContent(String.format(Strings.getString("command.config.updated"), keyOption.get().getAsString(), valueOption.get().getAsString())).queue();
        } else {
            context.replyCallbackAction().setContent(Strings.getString("command.config.unknown_variable")).queue();
        }
    }

    @Override
    public String getName() {
        return "config";
    }

    @Override
    public Supplier<String> getDescription() {
        return () -> Strings.getString("command.config.description");
    }

    @NotNull
    @Override
    public List<OptionData> getOptions() {
        return Arrays.asList(
                new OptionData(OptionType.STRING, "key", Strings.getString("command.config.option.key.description"), false),
                new OptionData(OptionType.STRING, "value", Strings.getString("command.config.option.value.description"), false)
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
        return () -> Strings.getString("command.config.help").formatted(
                Config.getDefaultConfiguration().keySet()
                        .stream()
                        .map(k -> "`%s`: %s".formatted(k, Config.getGuildVariable(guildId, k)))
                        .reduce("%s\n\t%s"::formatted)
                        .orElse("")
        );
    }

}
