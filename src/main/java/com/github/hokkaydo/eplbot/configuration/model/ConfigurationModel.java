package com.github.hokkaydo.eplbot.configuration.model;

/**
 * This class represents a configuration model.
 * @param key the key of the configuration entry
 * @param value the value of the configuration entry
 * @param guildId the id of the guild this configuration entry belongs to
 * @param state the state of the configuration entry (0 = configuration, 1 = state)
 * @see com.github.hokkaydo.eplbot.configuration.Config
 * */
public record ConfigurationModel(String key, String value, Long guildId, int state) {

}
