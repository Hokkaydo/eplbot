package com.github.hokkaydo.eplbot.database;

import java.util.List;

/**
 * This interface represents a CRUD repository.
 * <br>
 * CRUD stands for Create, Read, Update, Delete.
 * @param <M> the model type
 * */
public interface CRUDRepository<M> {

    /**
     * Creates a new model in the database.
     * @param models the models to create
     * */
    @SuppressWarnings("unchecked")
    void create(M... models);

    /**
     * Reads all models from the database.
     * @return a {@link List<M>} containing all models
     * */
    List<M> readAll();

    /**
     * Updates a given model in the database.
     * @param oldModel the old model
     * @param newModel the new model
     * */
    default void update(M oldModel, M newModel) {}

    /**
     * Deletes a given model from the database.
     * @param model the model to delete
     * */
    default void delete(M model) {}

}
