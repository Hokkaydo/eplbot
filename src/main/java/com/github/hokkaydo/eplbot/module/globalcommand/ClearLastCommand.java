package com.github.hokkaydo.eplbot.module.globalcommand;

import com.github.hokkaydo.eplbot.Strings;
import com.github.hokkaydo.eplbot.command.Command;
import com.github.hokkaydo.eplbot.command.CommandContext;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class ClearLastCommand implements Command {

    @Override
    public void executeCommand(CommandContext context) {
        Optional<OptionMapping> amountOption = context.options().stream().filter(o -> o.getName().equals("amount")).findFirst();
        if(amountOption.isEmpty()) return;
        int amount = amountOption.get().getAsInt();
        if(amount <= 0) {
            context.replyCallbackAction().setContent(Strings.getString("command.clear.amount_positive")).queue();
            return;
        }
        if(amount > 1)
            context.channel().getHistoryBefore(context.channel().getLatestMessageIdLong(), amount-1)
                    .map(MessageHistory::getRetrievedHistory)
                    .queue(l -> l.stream().map(Message::delete).forEach(AuditableRestAction::queue));
        context.channel().deleteMessageById(context.channel().getLatestMessageIdLong()).queue(
                ignored -> context.replyCallbackAction().setContent(Strings.getString("command.clear.processing")).queue(),
                ignored -> context.replyCallbackAction().setContent(Strings.getString("command.clear.message_too_old")).queue()
        );
    }

    @Override
    public String getName() {
        return "clearlast";
    }

    @Override
    public Supplier<String> getDescription() {
        return () -> Strings.getString("command.clear.last.description");
    }

    @NotNull
    @Override
    public List<OptionData> getOptions() {
        return Collections.singletonList(new OptionData(OptionType.STRING, "amount", Strings.getString("command.clear.last.option.amount.description"), true));
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
        return true;
    }

    @Override
    public Supplier<String> help() {
        return () -> Strings.getString("command.clear.last.help");
    }

}
