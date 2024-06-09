package com.github.hokkaydo.eplbot.module.shop;

import com.github.hokkaydo.eplbot.Strings;
import com.github.hokkaydo.eplbot.command.Command;
import com.github.hokkaydo.eplbot.command.CommandContext;
import com.github.hokkaydo.eplbot.module.shop.model.ShopItem;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;
import java.util.function.Supplier;

public class RemoveItemCommand implements Command {


        private final ShopProcessor processor;


        public RemoveItemCommand(ShopProcessor processor) {
            this.processor = processor;
        }
        @Override
        public void executeCommand(CommandContext context) {
            if (context.interaction().getGuild() == null) return;
            int id = context.options().getFirst().getAsInt();
            List<ShopItem> shop = this.processor.getShop();
            for (ShopItem item : shop) {
                if (item.id() == id) {
                    this.processor.removeItem(id);
                    context.replyCallbackAction().setContent(Strings.getString("REMOVE_ITEM_COMMAND_SUCCESSFUL")).queue();
                    return;
                }
            }
            context.replyCallbackAction().setContent(Strings.getString("REMOVE_ITEM_COMMAND_FAILURE")).queue();


        }

        @Override
        public String getName() {
            return "removeitem";
        }

    @Override
    public Supplier<String> getDescription() {
        return () -> Strings.getString( "REMOVE_ITEM_COMMAND_DESCRIPTION");
    }

    @Override
    public List<OptionData> getOptions() {

            return List.of(new OptionData(OptionType.INTEGER, "id", Strings.getString("REMOVE_ITEM_COMMAND_OPTION_ID_DESCRIPTION"), true)
            );
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
        return () -> Strings.getString("REMOVE_ITEM_COMMAND_HELP");
    }




}
