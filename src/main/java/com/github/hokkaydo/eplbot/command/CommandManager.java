package com.github.hokkaydo.eplbot.command;

import com.github.hokkaydo.eplbot.Main;
import com.github.hokkaydo.eplbot.Strings;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class handles command registration and execution.
 * */
public class CommandManager extends ListenerAdapter {

    private final Map<Long, Map<Class<? extends Command>, Boolean>> commandStatus = new HashMap<>();
    private final Map<Long, Map<String, Command>> commands = new HashMap<>();
    private final Map<String, Command> globalCommands = new HashMap<>();
    private final Map<Class<? extends Command>, Boolean> globalCommandStatus = new HashMap<>();

    /**
     * Disables the given commands for the given guild.
     * @param guildId the id of the guild
     * @param commands a {@link List<Command>} of {@link Command} to disable
     */
    public void disableCommands(Long guildId, List<Class<? extends Command>> commands) {
        if(!this.commandStatus.containsKey(guildId)) return;
        Map<Class<? extends Command>, Boolean> status = this.commandStatus.get(guildId);
        for (Class<? extends Command> command : commands) {
            status.put(command, false);
        }
        this.commandStatus.put(guildId, status);
    }

    /**
     * Enables the given commands for the given guild.
     * @param guildId the id of the guild
     * @param commands a {@link List<Command>} of {@link Command} to enable
     */
    public void enableCommands(Long guildId, List<Class<? extends Command>> commands) {
        Map<Class<? extends Command>, Boolean> status = this.commandStatus.getOrDefault(guildId, new HashMap<>());
        for (Class<? extends Command> command : commands) {
            status.put(command, true);
        }
        this.commandStatus.put(guildId, status);
    }

    /**
     * Enables the given commands globally.
     * @param commands a {@link List<Command>} of {@link Command} to disable
     */
    public void enableGlobalCommands(List<Class<? extends Command>> commands) {
        for (Class<? extends Command> command : commands) {
            this.globalCommandStatus.put(command, true);
        }
    }

    /**
     * Adds the given commands to the given guild.
     * @param commands a {@link List<Command>} of {@link Command} to enable
     */
    public void addCommands(Guild guild, List<Command> commands) {
        Map<String, Command> guildCommands = this.commands.getOrDefault(guild.getIdLong(), new HashMap<>());
        for (Command command : commands) {
            guildCommands.put(command.getName(), command);
        }
        guild.retrieveCommands().queue(s -> {
            if(s.size() == commands.size()) return;
            guild.updateCommands().addCommands(guildCommands.values().stream().map(this::mapToCommandData).toList()).queue();
        });
        this.commands.put(guild.getIdLong(), guildCommands);
    }

    /**
     * Maps a {@link Command} to a {@link CommandData} object.
     * Used in {@link #onSlashCommandInteraction(SlashCommandInteractionEvent)}
     * to pass the command to the command executor.
     * @param cmd the command to map
     * */
    private CommandData mapToCommandData(Command cmd) {
        return Commands.slash(cmd.getName(), cmd.getDescription().get())
                .addOptions(cmd.getOptions())
                .setDefaultPermissions(cmd.adminOnly() ? DefaultMemberPermissions.DISABLED : DefaultMemberPermissions.ENABLED);
    }

    /**
     * Command handler for slash commands.
     * This method is called whenever a slash command is executed.
     * It checks
      <ul>
        <li>if the command exists in the system</li>
        <li>if the owning module is enabled</li>
        <li>if the command is in the right channel</li>
        <li>if the user has the permission to execute the command</li>
     </ul>
     * Then it forwards the execution to the command executor.
     * @param event the {@link SlashCommandInteractionEvent} to handle
     */
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        Command command;
        if(!event.isGuildCommand() || event.getGuild() == null) {
            command = globalCommands.get(event.getFullCommandName());
            if(command == null) {
                event.reply(Strings.getString("command.not_found")).setEphemeral(true).queue();
                return;
            }
            if(Boolean.FALSE.equals(globalCommandStatus.getOrDefault(command.getClass(), false))) {
                event.reply(Strings.getString("command.disabled")).setEphemeral(true).queue();
                return;
            }
        }else {
            if(event.getGuild() == null) return;
            command = commands.getOrDefault(event.getGuild().getIdLong(), new HashMap<>()).getOrDefault(event.getFullCommandName(), null);
            if(command == null) {
                event.reply(Strings.getString("command.not_found")).setEphemeral(true).queue();
                return;
            }
            if(Boolean.FALSE.equals(commandStatus.getOrDefault(event.getGuild().getIdLong(), new HashMap<>()).getOrDefault(command.getClass(), false))) {
                event.reply(Strings.getString("command.disabled")).setEphemeral(true).queue();
                return;
            }
        }

        if(!command.validateChannel(event.getMessageChannel())) {
            event.reply(Strings.getString("command.wrong_channel")).setEphemeral(true).queue();
            return;
        }
        command.executeCommand(new CommandContext(event.getName(),
                event.getOptions(),
                event.getUser(),
                event.getMember(),
                event.getMessageChannel(),
                event.getCommandType(),
                event.getInteraction(),
                event.getHook(),
                event.deferReply(command.ephemeralReply())
        ));
    }

    /**
     * @return a {@link List<Command>} of all commands in the given guild or an empty list if the guild has no commands
     * */
    public List<Command> getCommands(Long guildId) {
        return new ArrayList<>(commands.getOrDefault(guildId, new HashMap<>()).values());
    }

    /**
     * Checks if a given module is enabled in the given guild.
     * @param guild the id of the guild
     * @param commandClazz the class of the command
     * @return true if the command is enabled, false otherwise
     */
    public boolean isEnabled(Long guild, Class<? extends Command> commandClazz) {
        return commandStatus.getOrDefault(guild, new HashMap<>()).getOrDefault(commandClazz, false);
    }

    /**
     * Add global commands to the bot.
     * @param commands a {@link List<Command>} of {@link Command} to add
     * */
    public void addGlobalCommands(List<Command> commands) {
        Map<String, Command> newGlobals = new HashMap<>(this.globalCommands);
        for (Command command : commands) {
            newGlobals.put(command.getName(), command);
        }
        Main.getJDA().retrieveCommands().queue(_ -> Main.getJDA().updateCommands().addCommands(newGlobals.values().stream().map(this::mapToCommandData).toList()).queue());
        this.globalCommands.clear();
        this.globalCommands.putAll(newGlobals);
    }

    /**
     * Refreshes the commands in the given guild.
     * @param guild the guild to refresh the commands in
     * */
    public void refreshCommands(Guild guild) {
        guild.updateCommands().addCommands(commands.get(guild.getIdLong()).values().stream().map(this::mapToCommandData).toList()).queue();
    }

}