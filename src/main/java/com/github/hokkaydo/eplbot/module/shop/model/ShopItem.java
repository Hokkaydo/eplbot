package com.github.hokkaydo.eplbot.module.shop.model;


public record ShopItem(
    int id,
    String name,
    int cost,
    String description,
    int type,
    float multiplier

){}
