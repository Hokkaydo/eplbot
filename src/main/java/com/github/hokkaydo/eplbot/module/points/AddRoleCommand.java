package com.github.hokkaydo.eplbot.module.points;

import com.github.hokkaydo.eplbot.Strings;
import com.github.hokkaydo.eplbot.command.Command;
import com.github.hokkaydo.eplbot.command.CommandContext;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;
import java.util.function.Supplier;

public class AddRoleCommand implements Command {


        private final PointsProcessor processor;


        public AddRoleCommand(PointsProcessor processor) {
            this.processor = processor;
        }
        @Override
        public void executeCommand(CommandContext context) {
            if (context.interaction().getGuild() == null) return;
            String role = context.options().getFirst().getAsRole().getName();
            role = STR."role_\{role}";
            int a = this.processor.addRole(role);
            if (a == -1) context.replyCallbackAction().setContent(Strings.getString("ADD_ROLE_COMMAND_ALREADY_EXISTS")).queue();
            else context.replyCallbackAction().setContent(Strings.getString("ADD_ROLE_COMMAND_SUCCESSFUL")).queue();


        }

        @Override
        public String getName() {
            return "addrole";
        }

    @Override
    public Supplier<String> getDescription() {
        return () -> Strings.getString( "ADD_ROLE_COMMAND_DESCRIPTION");
    }

    @Override
    public List<OptionData> getOptions() {

            return List.of(new OptionData(OptionType.ROLE, "role", Strings.getString("ADD_ROLE_COMMAND_OPTION_ROLE_DESCRIPTION"), true)

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
        return () -> Strings.getString("ADD_ROLE_COMMAND_HELP");
    }




}
