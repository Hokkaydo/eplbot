package com.github.hokkaydo.eplbot.module.mirror;

import com.github.hokkaydo.eplbot.Main;
import com.github.hokkaydo.eplbot.Strings;
import com.github.hokkaydo.eplbot.command.Command;
import com.github.hokkaydo.eplbot.command.CommandContext;
import com.github.hokkaydo.eplbot.module.mirror.model.MirrorLink;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class MirrorListCommand implements Command {

    private final MirrorManager mirrorManager;

    MirrorListCommand(MirrorManager mirrorManager) {
        this.mirrorManager = mirrorManager;
    }

    @Override
    public void executeCommand(CommandContext context) {
        Optional<OptionMapping> channelAOption = context.options().stream().filter(o -> o.getName().equals("channel")).findFirst();
        GuildMessageChannel channel;
        if(channelAOption.isEmpty()) {
            if(!context.channel().getType().isGuild() || !context.channel().getType().isMessage()) {
                context.replyCallbackAction().setContent(Strings.getString("command.mirror.list.channel_guild_text")).queue();
                return;
            }
            channel = (GuildMessageChannel) Main.getJDA().getGuildChannelById(ChannelType.TEXT, context.channel().getIdLong());
            if(channel == null) {
                context.replyCallbackAction().setContent(Strings.getString("command.mirror.list.channel_guild_text")).queue();
                return;
            }
        }else {
            channel = Main.getJDA().getChannelById(GuildMessageChannel.class, channelAOption.get().getAsString());
            if(channel == null) {
                context.replyCallbackAction().setContent(Strings.getString("command.mirror.invalid_channel_id").formatted(channelAOption.get().getAsString())).queue();
                return;
            }
        }
        List<MirrorLink> mirrors = mirrorManager.getLinks(channel);
        if(mirrors.isEmpty()) {
            context.replyCallbackAction().setContent(Strings.getString("command.mirror.list.no_mirror").formatted(channel.getAsMention())).queue();
            return;
        }
        StringBuilder stringBuilder = new StringBuilder("__Liste des liens existants__ :\n");
        for (MirrorLink link : mirrors) {
            MessageChannel first = link.first();
            MessageChannel second = link.second();
            stringBuilder.append("\t");
            stringBuilder.append(first.getAsMention()).append(" ");
            stringBuilder.append("(").append(link.first().getIdLong()).append(")").append(" <-> ");
            stringBuilder.append(second.getAsMention()).append(" ");
            stringBuilder.append("(").append(link.second().getIdLong()).append(")").append("\n");
        }
        context.replyCallbackAction().setContent(stringBuilder.toString()).queue();
    }

    @Override
    public String getName() {
        return "mirrorlist";
    }

    @Override
    public Supplier<String> getDescription() {
        return () -> Strings.getString("command.mirror.list.description");
    }

    @NotNull
    @Override
    public List<OptionData> getOptions() {
        return Collections.singletonList(new OptionData(OptionType.STRING, "channel", Strings.getString("command.mirror.list.option.channel.description"), false));
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
        return () -> Strings.getString("command.mirror.list.help");
    }

}
