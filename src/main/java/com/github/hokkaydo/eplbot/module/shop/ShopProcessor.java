package com.github.hokkaydo.eplbot.module.shop;

import com.github.hokkaydo.eplbot.Main;
import com.github.hokkaydo.eplbot.database.DatabaseManager;
import com.github.hokkaydo.eplbot.module.points.repository.PointsRepositorySQLite;
import com.github.hokkaydo.eplbot.module.shop.model.Inventory;
import com.github.hokkaydo.eplbot.module.shop.model.ShopItem;
import com.github.hokkaydo.eplbot.module.shop.repository.InventoryRepositorySQLite;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import javax.sql.DataSource;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShopProcessor extends ListenerAdapter {



        private final long guildId;


        private final static Map<Integer, ShopItem> shop = new HashMap<>();
        private final PointsRepositorySQLite pointsRepo;
        private final InventoryRepositorySQLite inventoryRepo;
        private List<String> roles;


        public ShopProcessor(long guildId) {
            super();
            Main.getJDA().addEventListener(this);
            DataSource datasource = DatabaseManager.getDataSource();
            this.guildId = guildId;
            this.pointsRepo = new PointsRepositorySQLite(datasource);
            this.inventoryRepo = new InventoryRepositorySQLite(datasource);
            this.roles = pointsRepo.getRoles();
            try {
                loadShop();
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }



        public static void loadShop() throws JSONException {
            InputStream stream = ShopProcessor.class.getClassLoader().getResourceAsStream("shop.json");
            assert stream != null;
            JSONArray items = new JSONArray(new JSONTokener(stream));
            if (items.isEmpty()) {
                System.out.println("The 'items' array in 'shop.json' is empty.");
                return;
            }

            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i);
                shop.put(item.getInt("id"), new ShopItem(
                        item.getInt("id"),
                        item.getString("name"),
                        item.getInt("cost"),
                        item.getString("description"),
                        item.getInt("type"),
                        (float) item.getDouble("multiplier")
                ));
            }
            System.out.println("Shop loaded.");

        }
        public void getItems(String role) {

        }
        public void addShopItem(ShopItem item) {

            InputStream stream = ShopProcessor.class.getClassLoader().getResourceAsStream("shop.json");
            assert stream != null;
            JSONArray jsonArray = new JSONArray(new JSONTokener(stream));
            JSONObject object = new JSONObject();
            object.put("id", item.id());
            object.put("name", item.name());
            object.put("cost", item.cost());
            object.put("description", item.description());
            object.put("type", item.type());
            object.put("multiplier", item.multiplier());
            jsonArray.put(object);
            System.out.println(jsonArray.toString(2));
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("src/main/resources/shop.json"))) {
                writer.write(jsonArray.toString(2));
            } catch (IOException e) {
                e.printStackTrace();
            }

            shop.put(item.id(), item);
        }


        public List<ShopItem> getShop() {
            return List.copyOf(shop.values());
        }

        public ShopItem getItem(int id) {
            return shop.get(id);
        }

        public void removeItem(int id) {
            shop.remove(id);
            writeShop();
        }

        public void writeShop() {
            JSONArray jsonArray = new JSONArray();
            for (ShopItem item : shop.values()) {
                JSONObject object = new JSONObject();
                object.put("id", item.id());
                object.put("name", item.name());
                object.put("cost", item.cost());
                object.put("description", item.description());
                object.put("type", item.type());
                object.put("multiplier", item.multiplier());
                jsonArray.put(object);
            }
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("src/main/resources/shop.json"))) {
                writer.write(jsonArray.toString(2));
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("An error occurred while writing the shop.");
            }
        }

        public String showInventory(String username) {
            Inventory inventory = inventoryRepo.getInventory(username);
            if (inventory.items().isEmpty()) {
                return "Votre inventaire est vide.";
            }
            StringBuilder builder = new StringBuilder();
            builder.append("Votre inventaire :\n");
            for (Map.Entry<Integer, Integer> entry : inventory.items().entrySet()) {
                ShopItem item = getItem(entry.getKey());
                builder.append(item.name()).append(" x").append(entry.getValue()).append("\n");
            }
            return builder.toString();
        }

        public void addItemToInventory(String username, ShopItem item){
            Inventory inventory = inventoryRepo.getInventory(username);
            Map<Integer, Integer> items = inventory.items();
            int new_capacity = inventory.capacity() - 1;
            items.put(item.id(),1+items.get(item.id()));
            this.inventoryRepo.updateInventory(items, username, new_capacity);

        }




}
