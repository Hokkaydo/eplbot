package com.github.hokkaydo.eplbot.module.shop.repository;

import com.github.hokkaydo.eplbot.module.shop.model.Inventory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InventoryRepositorySQLite implements InventoryRepository {
    private final JdbcTemplate jdbcTemplate;

    private static final RowMapper<Inventory> mapper = (resultSet, i) -> {
        String username = resultSet.getString("username");
        int capacity = resultSet.getInt("capacity");
        String items = resultSet.getString("items");
        Map<Integer, Integer> itemsList = convertStringToList(items);
        return new Inventory(username, capacity, itemsList);
    };

    public InventoryRepositorySQLite(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);

    }

    public static Map<Integer,Integer> convertStringToList(String items) {
        if (items.isEmpty()){
            return new HashMap<>();
        }
        String[] itemsSplit = items.split(",");
        Map<Integer, Integer> itemsList = new HashMap<>();
        for (String item : itemsSplit) {
            String[] idQty = item.split("-");
            itemsList.put(Integer.parseInt(idQty[0]), Integer.parseInt(idQty[1]));
        }
        return itemsList;
    }

    public static String convertListToString(Map<Integer, Integer> items) {
        StringBuilder itemsString = new StringBuilder();
        if (items.isEmpty()) {
            return "";
        }
        for (Map.Entry<Integer, Integer> entry : items.entrySet()) {
            itemsString.append(entry.getKey()).append("-").append(entry.getValue()).append(",");
        }
        //Remove the last comma
        itemsString.deleteCharAt(itemsString.length() - 1);
        return itemsString.toString();
    }
    @Override
    public void create(Inventory... models) {
        for (Inventory model : models) {
            jdbcTemplate.update("""
                    INSERT INTO inventories (
                        username,
                        capacity,
                        items
                        )
                    VALUES (?,?,?)
                    """, model.username(), model.capacity(), convertListToString(model.items()));


        }
    }

    @Override
    public List<Inventory> readAll() {
        return jdbcTemplate.query("SELECT * FROM inventories", mapper);

    }

    public Inventory getInventory(String username) {
        List<Inventory> userInventory = jdbcTemplate.query(
                "SELECT * FROM inventories WHERE username = ?",
                mapper,
                username
        );

        if (userInventory.isEmpty()) {
            jdbcTemplate.update("""
                    INSERT INTO inventories (
                        username,
                        capacity,
                        items
                        )
                    VALUES (?,?,?)
                    """, username, 10, "");
            return new Inventory(username, 10, new HashMap<>());
        }
        return userInventory.getFirst();
    }

    public void updateInventory(Map<Integer,Integer> items, String username, int new_capacity) {
        String inventory = convertListToString(items);
        jdbcTemplate.update("""
                UPDATE inventories
                SET items = ?,
                capacity = ?
                WHERE username = ?
                """, inventory, new_capacity, username);
    }
}
