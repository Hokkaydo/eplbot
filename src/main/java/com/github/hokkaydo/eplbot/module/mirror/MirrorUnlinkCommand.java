package com.github.hokkaydo.eplbot.module.mirror;

import com.github.hokkaydo.eplbot.Strings;
import com.github.hokkaydo.eplbot.command.Command;
import com.github.hokkaydo.eplbot.command.CommandContext;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class MirrorUnlinkCommand implements Command {

    private final MirrorManager mirrorManager;
    MirrorUnlinkCommand(MirrorManager mirrorManager) {
        this.mirrorManager = mirrorManager;
    }
    @Override
    public void executeCommand(CommandContext context) {
        Map.Entry<GuildMessageChannel, GuildMessageChannel> channels = MirrorModule.validateChannels(context);
        if(channels == null) return;
        GuildMessageChannel channelA = channels.getKey();
        GuildMessageChannel channelB = channels.getValue();

        if(!mirrorManager.existsLink(channelA, channelB)) {
            context.replyCallbackAction().setContent(Strings.getString("command.mirror.unlink.link_doesnt_exists").formatted(channelA.getAsMention(), channelB.getAsMention())).queue();
            return;
        }
        mirrorManager.destroyLink(channelA, channelB);
        context.replyCallbackAction().setContent(Strings.getString("command.mirror.unlink.link_destroyed").formatted(channelA.getAsMention(), channelB.getAsMention())).queue();
    }

    @Override
    public String getName() {
        return "mirrorunlink";
    }

    @Override
    public Supplier<String> getDescription() {
        return () -> Strings.getString("command.mirror.unlink.description");
    }

    @NotNull
    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.STRING, "channel_a", Strings.getString("command.mirror.unlink.option.channel_a.description"), true),
                new OptionData(OptionType.STRING, "channel_b", Strings.getString("command.mirror.unlink.option.channel_b.description"), true)
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
        return () -> Strings.getString("command.mirror.unlink.help");
    }

}
