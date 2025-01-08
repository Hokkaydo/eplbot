package com.github.hokkaydo.eplbot.database;


import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

public class SQLiteDatasourceFactory {

    private SQLiteDatasourceFactory() {}

    /**
     * Creates a {@link DataSource} for a SQLite database.
     * @param path the path to the SQLite database
     * @return a {@link DataSource} for a SQLite database
     * */
    public static DataSource create(String path) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl("jdbc:sqlite:" + path);
        return dataSource;
    }
}
