package com.github.hokkaydo.eplbot.module.graderetrieve;

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

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class SetupRetrieveChannelCommand implements Command {

    private final Long guildId;
    private final ExamsRetrieveListener examsRetrieveListener;
    SetupRetrieveChannelCommand(Long guildId, ExamsRetrieveListener examsRetrieveListener) {
        this.guildId = guildId;
        this.examsRetrieveListener = examsRetrieveListener;
    }

    @Override
    public void executeCommand(CommandContext context) {
        Optional<OptionMapping> quarterOpt = context.options().stream().filter(e -> e.getName().equals("quarter")).findFirst();
        if(quarterOpt.isEmpty()) return;
        Config.updateValue(guildId, "EXAM_ZIP_MESSAGE_ID", "");
        File zip = new File(ExamsRetrieveListener.ZIP_PATH.toUri());
        if(zip.exists())
            zip.delete();
        examsRetrieveListener.setGradeRetrieveChannelId(context.channel().getIdLong(), quarterOpt.get().getAsInt());
        Config.updateValue(guildId, "EXAM_RETRIEVE_CHANNEL_ID", context.channel().getId());
        context.replyCallbackAction().setContent("Processing!").queue();
    }

    @Override
    public String getName() {
        return "setupgradechannel";
    }

    @Override
    public Supplier<String> getDescription() {
        return () -> Strings.getString("command.setup_grade_channel.description");
    }

    @NotNull
    @Override
    public List<OptionData> getOptions() {
        return Collections.singletonList(new OptionData(OptionType.INTEGER,"quarter", "Quadrimestre", true).addChoice("1", 1).addChoice("2", 2));
    }

    @Override
    public boolean ephemeralReply() {
        return true;
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
        return () -> Strings.getString("command.setup_grade_channel.help");
    }

}
