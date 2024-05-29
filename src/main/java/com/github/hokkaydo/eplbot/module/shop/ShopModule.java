package com.github.hokkaydo.eplbot.module.shop;

import com.github.hokkaydo.eplbot.command.Command;
import com.github.hokkaydo.eplbot.module.Module;
import com.github.hokkaydo.eplbot.module.points.PointsProcessor;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class ShopModule extends Module{

        private final ShopProcessor processor;
        private final PointsProcessor ptsProcessor;
        private final ShopCommand shopCommand;
        private final AddItemCommand addItemCommand;
        private final RemoveItemCommand removeItemsCommand;
        private final InventoryCommand inventoryCommand;
        private final BuyCommand buyCommand;




        public ShopModule(@NotNull Long guildId) {
            super(guildId);

            this.processor = new ShopProcessor(guildId);
            this.ptsProcessor = new PointsProcessor(guildId);
            this.shopCommand = new ShopCommand(this.processor);
            this.addItemCommand = new AddItemCommand(this.processor);
            this.removeItemsCommand = new RemoveItemCommand(this.processor);
            this.inventoryCommand = new InventoryCommand(this.processor);
            this.buyCommand = new BuyCommand(this.processor, this.ptsProcessor);


        }

        @Override
        public String getName() {
            return "shop";
        }

        @Override
        public List<Command> getCommands() {
            return List.of(
                    shopCommand,
                    addItemCommand,
                    removeItemsCommand,
                    inventoryCommand,
                    buyCommand
            );
        }


        @Override
        public List<ListenerAdapter> getListeners() {
            return List.of(processor);
        }

        public List<Command> getGlobalCommands() {
            return Collections.emptyList();
        }


}
