package com.github.hokkaydo.eplbot.command;


import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;

import java.util.List;
import java.util.Optional;

/**
 * Represents the context of a command.
 * This class is used to pass information
 * about the command to the command executor.
 * */
public record CommandContext(String commandName, List<OptionMapping> options,
                             User user,
                             Member author, MessageChannel channel,
                             Command.Type commandType, SlashCommandInteraction interaction, InteractionHook hook,
                             ReplyCallbackAction replyCallbackAction) {

    /**
     * Shortcut to get an option by its name.
     * @param name the name of the option
     * @return an {@link Optional} containing the option if it exists, an empty optional otherwise
     */
    public Optional<OptionMapping> getOption(String name) {
        return options.stream()
                      .filter(option -> option.getName().equals(name))
                      .findFirst();
    }

}