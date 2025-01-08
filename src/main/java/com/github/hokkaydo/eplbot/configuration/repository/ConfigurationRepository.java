package com.github.hokkaydo.eplbot.configuration.repository;

import com.github.hokkaydo.eplbot.configuration.model.ConfigurationModel;
import com.github.hokkaydo.eplbot.database.CRUDRepository;

import java.util.List;

/**
 * This interface represents a repository for configuration entries.
 * <br>
 * For more information about the distinction between configuration and state entries,
 * see {@link com.github.hokkaydo.eplbot.configuration.Config}
 * @see com.github.hokkaydo.eplbot.configuration.Config
 * */
public interface ConfigurationRepository extends CRUDRepository<ConfigurationModel> {

    /**
     * Updates the value of a configuration entry.
     * @param guildId the id of the guild
     * @param key the key of the configuration entry
     * @param value the value of the configuration entry
     * */
    void updateGuildVariable(Long guildId, String key, String value);

    /**
     * Updates the value of a state entry.
     * @param guildId the id of the guild
     * @param key the key of the state entry
     * @param value the value of the state entry
     * */
    void updateGuildState(Long guildId, String key, String value);

    /**
     * Gets all configuration <b>state</b> entries
     * @return a {@link List<ConfigurationModel>} containing all configuration entries
     * */
    List<ConfigurationModel> getGuildStates();

    /**
     * Gets all configuration <b>variable</b> entries
     * @return a {@link List<ConfigurationModel>} containing all state entries
     * */
    List<ConfigurationModel> getGuildVariables();

}
