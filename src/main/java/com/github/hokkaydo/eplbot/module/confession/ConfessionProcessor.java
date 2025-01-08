package com.github.hokkaydo.eplbot.module.confession;

import com.github.hokkaydo.eplbot.Main;
import com.github.hokkaydo.eplbot.MessageUtil;
import com.github.hokkaydo.eplbot.Strings;
import com.github.hokkaydo.eplbot.command.CommandContext;
import com.github.hokkaydo.eplbot.configuration.Config;
import com.github.hokkaydo.eplbot.module.confession.model.WarnedConfession;
import com.github.hokkaydo.eplbot.module.confession.repository.WarnedConfessionRepository;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.awt.*;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class ConfessionProcessor extends ListenerAdapter {

    private final Map<UUID, MessageCreateBuilder> confessions = new HashMap<>();
    private final Map<UUID, String> confessionsContent = new HashMap<>();
    private final List<UUID> confessFollowing = new ArrayList<>();
    private final Map<UUID, Long> confessionAuthor = new HashMap<>();
    private static final String[] VALIDATION_EMBED_TITLES = {"Confession - Validée", "Confession - Refusée", "Confession - Signalée"};
    private static final Color[] VALIDATION_EMBED_COLORS = {Color.GREEN, Color.RED, Color.YELLOW};
    private static final int VALID = 0;
    private static final int REFUSED = 1;
    private static final int WARNED = 2;

    private static final String CONFESSION = "confession";

    private final Long guildId;
    private final Map<Long, Long> lastMainConfession;
    private final Map<Long, Long> lastMainConfessionValidation;
    private final Map<Long, Long> lastFollowingConfessionValidation;
    private final WarnedConfessionRepository warnedConfessionRepository;

    ConfessionProcessor(Long guildId, Map<Long, Long> lastMainConfession, WarnedConfessionRepository warnedConfessionRepository) {
        this.guildId = guildId;
        this.lastMainConfession = lastMainConfession;
        this.lastFollowingConfessionValidation = new HashMap<>();
        this.lastMainConfessionValidation = new HashMap<>();
        this.warnedConfessionRepository = warnedConfessionRepository;
    }

    void process(CommandContext context, boolean following) {
        String confessionValidationChannelId = Config.getGuildVariable(guildId, "CONFESSION_VALIDATION_CHANNEL_ID");
        if(confessionValidationChannelId.isBlank()) {
            MessageUtil.sendAdminMessage(Strings.getString("WARNING_CONFESSION_VALIDATION_CHANNEL_ID_INVALID"), guildId);
            context.replyCallbackAction().setContent(Strings.getString("ERROR_OCCURRED")).queue();
            return;
        }
        TextChannel validationChannel = Main.getJDA().getChannelById(TextChannel.class, confessionValidationChannelId);
        if(validationChannel == null) {
            MessageUtil.sendAdminMessage(Strings.getString("WARNING_CONFESSION_VALIDATION_CHANNEL_ID_INVALID"), guildId);
            context.replyCallbackAction().setContent(Strings.getString("ERROR_OCCURRED")).queue();
            return;
        }
        Optional<OptionMapping> confessionOption = context.options().stream().filter(o -> o.getName().equals(CONFESSION)).findFirst();
        if(confessionOption.isEmpty()) return;
        String confession = confessionOption.get().getAsString();

        if(confession.length() > MessageEmbed.DESCRIPTION_MAX_LENGTH) {
            context.replyCallbackAction().applyData(MessageCreateData.fromContent(Strings.getString("COMMAND_CONFESSION_TOO_LONG").formatted(MessageEmbed.DESCRIPTION_MAX_LENGTH))).queue();
            return;
        }

        UUID confessUUID = UUID.randomUUID();
        context.replyCallbackAction().applyData(MessageCreateData.fromContent(Strings.getString("COMMAND_CONFESSION_SUBMITTED"))).queue();

        MessageCreateBuilder embedBuilder = MessageCreateBuilder.from(MessageCreateData.fromEmbeds(
                new EmbedBuilder()
                        .setColor(Config.getGuildVariable(guildId, "CONFESSION_EMBED_COLOR"))
                        .setDescription(confession)
                        .setTitle("Confession")
                        .setTimestamp(Instant.now())
                        .build()
        ));
        confessions.put(confessUUID, embedBuilder);
        confessionsContent.put(confessUUID, confessionOption.get().getAsString());
        confessionAuthor.put(confessUUID, context.user().getIdLong());
        MessageCreateBuilder data = MessageCreateBuilder.from(embedBuilder.build())
                                            .addActionRow(
                                                    Button.primary("validate-confession;" + confessUUID, Emoji.fromUnicode("✅")),
                                                    Button.primary("warn-confession;" + confessUUID, Emoji.fromUnicode("⚠")),
                                                    Button.primary("refuse-confession;" + confessUUID, Emoji.fromUnicode("❌"))
                                            );
        if(following) {
            confessFollowing.add(confessUUID);
            if(lastFollowingConfessionValidation.containsKey(context.user().getIdLong())) {
                validationChannel.retrieveMessageById(lastFollowingConfessionValidation.get(context.user().getIdLong())).queue(lastValidation -> lastValidation.reply(data.build()).queue());
                return;
            }
            validationChannel.retrieveMessageById(lastMainConfessionValidation.get(context.user().getIdLong())).queue(lastValidation -> lastValidation.reply(data.build()).queue(m -> lastFollowingConfessionValidation.put(context.user().getIdLong(), m.getIdLong())));
            return;
        }
        validationChannel.sendMessage(data.build()).queue(m -> lastMainConfessionValidation.put(context.user().getIdLong(), m.getIdLong()));
    }

    private void sendConfession(UUID uuid, Long guildId) {
        MessageCreateBuilder confession = confessions.get(uuid);
        confessions.remove(uuid);
        if(confession == null) return;

        String confessionChannelId = Config.getGuildVariable(guildId, "CONFESSION_CHANNEL_ID");
        if(confessionChannelId.isBlank()) {
            MessageUtil.sendAdminMessage(Strings.getString("WARNING_CONFESSION_CHANNEL_ID_INVALID"), guildId);
            return;
        }

        TextChannel confessionChannel = Main.getJDA().getChannelById(TextChannel.class, confessionChannelId);
        if(confessionChannel == null) {
            MessageUtil.sendAdminMessage(Strings.getString("WARNING_CONFESSION_CHANNEL_ID_INVALID"), guildId);
            return;
        }
        if(confessFollowing.contains(uuid)) {
            confessionChannel.retrieveMessageById(lastMainConfession.get(confessionAuthor.get(uuid))).queue(m -> {
                ThreadChannel channel = m.getStartedThread();
                if(channel == null) {
                    m.createThreadChannel("Confession - Follow").queue(t -> {
                        if(t == null) {
                            MessageUtil.sendAdminMessage(Strings.getString("WARNING_CONFESSION_CHANNEL_ID_INVALID"), guildId);
                            return;
                        }
                        t.sendMessage(confession.build()).queue();
                    });
                    return;
                }
                channel.sendMessage(confession.build()).queue();
            });
            confessFollowing.remove(uuid);
            return;
        }
        Long authorId = confessionAuthor.get(uuid);
        confessionAuthor.remove(uuid);
        lastFollowingConfessionValidation.remove(authorId);
        confessionChannel.sendMessage(confession.build()).queue(m -> lastMainConfession.put(authorId, m.getIdLong()));
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String id = event.getButton().getId();
        if(id == null || !id.contains(CONFESSION)) return;
        if(event.getGuild() == null || event.getGuild().getIdLong() != guildId) return;
        event.getInteraction().deferEdit().queue();
        event.getHook().editOriginalComponents(Collections.emptyList()).queue();
        UUID uuid = UUID.fromString(id.split(";")[1]);
        if(id.startsWith("validate")) {
            updateValidationEmbedColor(VALID, event.getHook(), event.getMessage());
            sendConfession(uuid, event.getGuild().getIdLong());
        } else if(id.startsWith("refuse")){
            updateValidationEmbedColor(REFUSED, event.getHook(), event.getMessage());
        } else {
            updateValidationEmbedColor(WARNED, event.getHook(), event.getMessage());
            warn(event.getUser().getIdLong(), uuid);
        }
    }

    private void warn(Long moderatorId, UUID uuid) {
        Long confessionAuthorId = confessionAuthor.get(uuid);
        confessionAuthor.remove(uuid);
        WarnedConfession warnedConfession = new WarnedConfession(moderatorId, confessionAuthorId, confessionsContent.get(uuid) , Timestamp.from(Instant.now()));
        warnedConfessionRepository.create(warnedConfession);
        confessionsContent.remove(uuid);
        int threshold = Config.<Integer>getGuildVariable(guildId, "CONFESSION_WARN_THRESHOLD");
        Guild guild = Main.getJDA().getGuildById(guildId);
        if(guild == null) return;

        List<WarnedConfession> authorWarnedConfessions = warnedConfessionRepository.readByAuthor(confessionAuthorId);
        Main.getJDA().retrieveUserById(confessionAuthorId).flatMap(User::openPrivateChannel).queue(privateChannel -> privateChannel.sendMessage(Strings.getString("COMMAND_CONFESSION_WARN_AUTHOR")).queue());

        if(authorWarnedConfessions.size() < threshold) return;
        TextChannel validationChannel = Main.getJDA().getChannelById(TextChannel.class, Config.getGuildVariable(guildId,"CONFESSION_VALIDATION_CHANNEL_ID"));
        if(validationChannel == null) {
            MessageUtil.sendAdminMessage(Strings.getString("WARNING_CONFESSION_VALIDATION_CHANNEL_ID_INVALID"), guildId);
            return;
        }
        Member member = guild.getMemberById(confessionAuthorId);
        String username = Optional.ofNullable(Main.getJDA().getUserById(confessionAuthorId)).map(User::getName).orElse("USER NOT ON SERVER: " + confessionAuthorId);
        EmbedBuilder builder = new EmbedBuilder()
                                       .setDescription(Strings.getString("CONFESSION_TOO_MUCH_WARNS")
                                                               .formatted(
                                                                       member == null ? username : member.getAsMention(),
                                                                       threshold,
                                                                       threshold,
                                                                       member == null ? "@" + username : "@" + member.getEffectiveName()
                                                               )
                                       )
                                       .setColor(Color.ORANGE);
        validationChannel.sendMessageEmbeds(builder.build()).queue(m -> {
            for (WarnedConfession confession : authorWarnedConfessions) {
                Member mod = guild.getMemberById(confession.moderatorId());
                EmbedBuilder warned = new EmbedBuilder()
                                              .setFooter(Strings.getString("CONFESSION_WARNED_BY").formatted(mod == null ? confession.moderatorId() : mod.getUser().getName()))
                                              .setDescription(confession.messageContent())
                                              .setColor(Color.BLACK)
                                              .setTimestamp(confession.timestamp().toInstant())
                                              .setAuthor(member == null ? String.valueOf(confessionAuthorId) : member.getUser().getName());
                m.replyEmbeds(warned.build()).queue();
            }
        });
    }
    private void updateValidationEmbedColor(int state, InteractionHook hook, Message message) {
        MessageEmbed embed = message.getEmbeds().getFirst();
        EmbedBuilder builder = EmbedBuilder.fromData(embed.toData()).setColor(VALIDATION_EMBED_COLORS[state]).setTitle(VALIDATION_EMBED_TITLES[state]);
        hook.editOriginalEmbeds(builder.build()).queue();
    }

    public void clearWarnings(long authorId) {
        warnedConfessionRepository.deleteByAuthor(authorId);
    }

}
