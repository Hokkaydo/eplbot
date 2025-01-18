package com.github.hokkaydo.eplbot.module.globalcommand;

import com.github.hokkaydo.eplbot.Strings;
import com.github.hokkaydo.eplbot.command.Command;
import com.github.hokkaydo.eplbot.command.CommandContext;
import com.github.hokkaydo.eplbot.configuration.Config;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class StateCommand implements Command {

    private final Long guildId;
    private static final String RESET = "reset";
    StateCommand(Long guildId) {
        this.guildId = guildId;
    }
    @Override
    public void executeCommand(CommandContext context) {
        Optional<OptionMapping> subCommand = context.options().stream().filter(s -> s.getName().equals("subcommand")).findFirst();
        if(subCommand.isEmpty()) {
            context.replyCallbackAction().setContent(
                    Config.getDefaultState().keySet().stream()
                            .map(k -> "`%s`: %s".formatted(k, Config.getGuildState(guildId, k)))
                            .reduce("%s%n%s"::formatted)
                            .orElse("")
            ).queue();
            return;
        }
        if(subCommand.get().getAsString().equalsIgnoreCase(RESET)) {
            Config.resetDefaultState(guildId);
            context.replyCallbackAction().setContent(Strings.getString("command.state.reset")).queue();
            return;
        }
        context.replyCallbackAction().setContent(Strings.getString("command.state.action_not_found").formatted(subCommand.get().getAsString())).queue();
    }

    @Override
    public String getName() {
        return "state";
    }

    @Override
    public Supplier<String> getDescription() {
        return () -> Strings.getString("command.state.description");
    }

    @NotNull
    @Override
    public List<OptionData> getOptions() {
        return List.of(new OptionData(OptionType.STRING, "subcommand", Strings.getString("command.state.option.subcommand.description"), false)
                               .addChoice(RESET, RESET));
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
        return () -> Strings.getString("command.state.help");
    }

}
