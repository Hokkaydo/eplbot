package com.github.hokkaydo.eplbot.module.shop;

import com.github.hokkaydo.eplbot.Strings;
import com.github.hokkaydo.eplbot.command.Command;
import com.github.hokkaydo.eplbot.command.CommandContext;
import com.github.hokkaydo.eplbot.module.points.PointsProcessor;
import com.github.hokkaydo.eplbot.module.shop.model.ShopItem;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;
import java.util.function.Supplier;



public class BuyCommand implements Command {

    private final ShopProcessor processor;
    private final PointsProcessor ptsProcessor;

    public BuyCommand(ShopProcessor processor, PointsProcessor ptsProcessor) {
        this.processor = processor;
        this.ptsProcessor = ptsProcessor;
    }
    @Override
    public void executeCommand(CommandContext context) {
        if (context.interaction().getGuild() == null) return;
        int id = context.options().getFirst().getAsInt();
        List<ShopItem> shop = this.processor.getShop();
        String username = context.interaction().getUser().getName();
        this.ptsProcessor.activateAuthor(context.author());
        String role = this.ptsProcessor.getRole(username);
        for (ShopItem item : shop) {
            if (item.id() == id) {
                if (this.ptsProcessor.getPoints(username) >= item.cost()) {
                    this.ptsProcessor.addPoints(username, -item.cost());
                    this.ptsProcessor.addPoints(STR."role_\{role}", -item.cost());
                    this.processor.addItemToInventory(username, item);
                    context.replyCallbackAction().setContent(Strings.getString("BUY_COMMAND_SUCCESSFUL")).queue();
                } else {
                    context.replyCallbackAction().setContent(Strings.getString("BUY_COMMAND_NOT_ENOUGH_POINTS")).queue();
                }
                return;
            }
        }
        context.replyCallbackAction().setContent(Strings.getString("BUY_COMMAND_FAILURE")).queue();
    }




    @Override
    public String getName() {
        return "buy";
    }

    @Override
    public Supplier<String> getDescription() {
        return () -> Strings.getString("BUY_COMMAND_DESCRIPTION");
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(new OptionData(OptionType.INTEGER, "id", Strings.getString("BUY_COMMAND_OPTION_ID_DESCRIPTION"), true));
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
        return () -> Strings.getString("BUY_COMMAND_HELP");
    }
}
