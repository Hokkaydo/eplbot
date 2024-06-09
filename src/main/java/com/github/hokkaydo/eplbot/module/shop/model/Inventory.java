package com.github.hokkaydo.eplbot.module.shop.model;

import java.util.Map;

public record Inventory(
        String username, int capacity, Map<Integer, Integer> items) {}
