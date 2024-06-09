package com.github.hokkaydo.eplbot.module.points;

import com.github.hokkaydo.eplbot.Strings;
import com.github.hokkaydo.eplbot.command.Command;
import com.github.hokkaydo.eplbot.command.CommandContext;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;
import java.util.function.Supplier;

public class PointsCommand implements Command {


        private final PointsProcessor processor;

        public PointsCommand(PointsProcessor processor) {
            this.processor = processor;
        }
        @Override
        public void executeCommand(CommandContext context) {
            if (context.interaction().getGuild() == null) return;
            String username;
            boolean isSelf = true;
            if (context.options().isEmpty()) {
                username = context.user().getName();
            } else if (context.options().getFirst().getType() == OptionType.USER) {
                username = context.options().getFirst().getAsUser().getName();
                isSelf = false;
            } else {
                username = STR."role_\{context.options().getFirst().getAsRole().getName()}";
                isSelf = false;
            }
            this.processor.activateAuthor(context.author());
            String value = String.valueOf(this.processor.getPoints(username));
            if (isSelf) {

            context.replyCallbackAction().setContent(STR."Vous avez actuellement \{value} points.").queue();
        }
            else if (username.startsWith("role_")) {

                username = username.substring(5);
                context.replyCallbackAction().setContent(STR."Le clan \{username} a actuellement \{value} points.").queue();
            }
            else {
                context.replyCallbackAction().setContent(STR."\{username} a actuellement \{value} points.").queue();
            }
        }

        @Override
        public String getName() {
            return "points";
        }

    @Override
    public Supplier<String> getDescription() {
        return () -> Strings.getString( "POINTS_COMMAND_DESCRIPTION");
    }

    @Override
    public List<OptionData> getOptions() {

            return List.of(new OptionData(OptionType.USER, "user", Strings.getString("POINTS_COMMAND_OPTION_USER_DESCRIPTION"), false), new OptionData(OptionType.ROLE, "role", Strings.getString("POINTS_COMMAND_OPTION_ROLE_DESCRIPTION"), false));
    }

    @Override
        public boolean ephemeralReply() {
            return true;
        }

        @Override
        public boolean validateChannel(MessageChannel channel) {
            return true;
        }

        @Override
        public boolean adminOnly() {
            return false;
        }

    @Override
    public Supplier<String> help() {
        return () -> Strings.getString("POINTS_COMMAND_HELP");
    }




}
