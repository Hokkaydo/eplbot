package com.github.hokkaydo.eplbot.database;

import java.util.Map;

/**
 * This class represents a table model.
 * @param name the name of the table
 * @param parameters the parameters of the table with their types (key = parameter name, value = parameter type)
 * */
public record TableModel(String name, Map<String, String> parameters) {

}
