package com.github.hokkaydo.eplbot.module.shop;

import com.github.hokkaydo.eplbot.Main;
import com.github.hokkaydo.eplbot.database.DatabaseManager;
import com.github.hokkaydo.eplbot.module.points.model.Points;
import com.github.hokkaydo.eplbot.module.points.repository.PointsRepositorySQLite;
import com.github.hokkaydo.eplbot.module.shop.model.Item;
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



        private long guildId;


        private static Map<Integer, Item> shop = new HashMap<>();
        private PointsRepositorySQLite pointsRepo;


        public ShopProcessor(long guildId) {
            super();
            Main.getJDA().addEventListener(this);
            DataSource datasource = DatabaseManager.getDataSource();
            this.guildId = guildId;
            this.pointsRepo = new PointsRepositorySQLite(datasource);
            try {
                loadRoles();
                loadShop();
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        public static void loadRoles() throws JSONException {

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
                shop.put(item.getInt("id"), new Item(
                        item.getInt("id"),
                        item.getString("name"),
                        item.getInt("cost"),
                        item.getString("description"),
                        item.getInt("type"),
                        (float) item.getDouble("multiplier")
                ));
            }

        }
        public void getItems(String role) {

        }
        public void addItem(Item item) {

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

        public int addRole(String role) {
        if (this.pointsRepo.getUser(role) != null) {
            return -1;
        }
        else {
            this.pointsRepo.create(new Points(role, 0, role.substring(5), 0, 0));
            return 0;
        }

        }
        public List<Item> getShop() {
            return List.copyOf(shop.values());
        }





}
