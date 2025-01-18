package com.github.hokkaydo.eplbot.module.globalcommand;

import com.github.hokkaydo.eplbot.Strings;
import com.github.hokkaydo.eplbot.command.Command;
import com.github.hokkaydo.eplbot.command.CommandContext;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class LMGTCommand implements Command {

    private static final String lmgtLink = "https://letmegooglethat.com/?q=";
    @Override
    public void executeCommand(CommandContext context) {
        if(context.options().isEmpty()) throw new IllegalStateException("Should not arise");
        String subject = context.options().getFirst().getAsString();
        context.replyCallbackAction().setContent(lmgtLink + URLEncoder.encode(subject, Charset.defaultCharset())).queue();
    }

    @Override
    public String getName() {
        return "lmgt";
    }

    @Override
    public Supplier<String> getDescription() {
        return () -> Strings.getString("command.lmgt.description");
    }

    @NotNull
    @Override
    public List<OptionData> getOptions() {
        return Collections.singletonList(new OptionData(OptionType.STRING, "sujet", Strings.getString("command.lmgt.option.subject.description"), true));
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
        return false;
    }

    @Override
    public Supplier<String> help() {
        return () -> Strings.getString("command.lmgt.help");
    }

}
