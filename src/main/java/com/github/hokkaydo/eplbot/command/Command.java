package com.github.hokkaydo.eplbot.command;

import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.function.Supplier;

/**
 * All commands must implement this interface.
 * It provides the basic structure for a command.
 * This allows it to be registered in the {@link CommandManager} and be enabled / disabled along its owning module.
 * */
public interface Command  {

    /**
     * Executes the command with the given context.
     * @param context the context of the command
     * */
    void executeCommand(CommandContext context);

    /**
     * @return the name of the command
     * */
    String getName();

    /**
     * @return the description of the command as a {@link Supplier<String>} to allow for dynamic descriptions
     * */
    Supplier<String> getDescription();

    /**
     * @return the arguments of the command as a list of {@link OptionData}. If the command has no options, returns an empty list.
     * */
    @NonNull List<OptionData> getOptions();

    /**
     * @return whether the command is ephemeral or not. If true, the reply will only be visible to the user who executed the command.
     * */
    boolean ephemeralReply();

    /**
     * Checks if the command is valid for the given channel.
     * Useful for commands that should only be used in certain channels such as private channels.
     * @return true if the command is valid for the given channel, false otherwise
     * */
    boolean validateChannel(MessageChannel channel);

    /**
     * Checks if the command is only available to users with ADMINISTRATOR permission.
     * @return true if the command is only available to administrators, false otherwise
     * */
    boolean adminOnly();

    /**
     * @return the help message of the command as a {@link Supplier<String>} to allow for dynamic help messages
     * */
    Supplier<String> help();

}