package com.github.hokkaydo.eplbot.module.messagebird;

import com.github.hokkaydo.eplbot.MessageUtil;
import com.github.hokkaydo.eplbot.Strings;
import com.github.hokkaydo.eplbot.command.Command;
import com.github.hokkaydo.eplbot.command.CommandContext;
import com.github.hokkaydo.eplbot.configuration.Config;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;

import java.util.List;
import java.util.function.Supplier;

public class MessageBirdNextMessageCommand extends ListenerAdapter implements Command {

    private final long guildId;
    // Should be using Message.MAX_CONTENT_LENGTH, but somehow it doesn't accept more than 1024 chars ¯\_(ツ)_/¯
    private static final int MAX_LENGTH = 1024;
    private final List<String> types;

    MessageBirdNextMessageCommand(long guildId, List<String> types) {
        this.guildId = guildId;
        this.types = types;
    }

    @Override
    public void executeCommand(CommandContext context) {
        if (!context.interaction().isGuildCommand() || context.interaction().getGuild() == null) return;
        String type = context.getOption("type").map(OptionMapping::getAsString).orElse(null);
        String roleId = Config.getGuildVariable(guildId, STR."\{type}_BIRD_ROLE_ID");
        if (context.author().getRoles().stream().filter(r -> r.getId().equals(roleId)).findFirst().isEmpty()) {
            context.replyCallbackAction().setContent(Strings.getString("MESSAGE_BIRD_NOT_VALID_BIRD")).queue();
            return;
        }
        Modal modal = Modal.create(STR."messagebird-\{type}", "Message")
                              .addActionRow(TextInput.create("message", "Message", TextInputStyle.PARAGRAPH).build())
                              .build();
        context.interaction().replyModal(modal).queue();
    }

    @Override
    public String getName() {
        return "messagebird";
    }

    @Override
    public Supplier<String> getDescription() {
        return () -> Strings.getString("MESSAGE_BIRD_NEXT_MESSAGE_COMMAND_DESCRIPTION");
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.STRING, "type", "Type", true)
                        .addChoices(types.stream().map(s -> new net.dv8tion.jda.api.interactions.commands.Command.Choice(s, s)).toList())
        );

    }

    @Override
    public boolean ephemeralReply() {
        return true;
    }

    @Override
    public boolean validateChannel(MessageChannel channel) {
        return channel instanceof GuildMessageChannel;
    }

    @Override
    public boolean adminOnly() {
        return false;
    }

    @Override
    public Supplier<String> help() {
        return () -> Strings.getString("MESSAGE_BIRD_NEXT_MESSAGE_COMMAND_HELP");
    }

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        if (event.getGuild() == null || event.getGuild().getIdLong() != guildId) return;
        String id = event.getModalId();
        if (!id.startsWith("messagebird")) return;
        String type = id.substring("messagebird-".length());
        ModalMapping contentMap = event.getInteraction().getValue("message");
        if (contentMap == null) return;
        String content = contentMap.getAsString();
        if (content.length() > MAX_LENGTH) {
            event.reply(Strings.getString("MESSAGE_BIRD_NEXT_MESSAGE_TOO_LONG").formatted(MAX_LENGTH)).queue();
            return;
        }
        Config.updateValue(event.getGuild().getIdLong(), STR."\{type}_BIRD_NEXT_MESSAGE", content);
        MessageUtil.sendAdminMessage(STR."Prochain message \{type} enregistré par %s :%n >>> %s".formatted(event.getUser().getAsMention(), content), event.getGuild().getIdLong());
        event.reply(Strings.getString("MESSAGE_BIRD_NEXT_MESSAGE_REGISTERED")).queue();
        event.getUser().openPrivateChannel().queue(dm -> dm.sendMessage(Strings.getString("MESSAGE_BIRD_NEXT_MESSAGE_REGISTERED_DM").formatted(content)).queue());
    }

}
