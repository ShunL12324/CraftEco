package com.github.ericliucn.crafteco.handler;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class DataSource {

    private final HikariDataSource dataSource;

    public DataSource(String jdbc, String user, String passwd){
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(jdbc);
        hikariConfig.setUsername(user);
        hikariConfig.setPassword(passwd);
        hikariConfig.setMaximumPoolSize(10);
        hikariConfig.setMinimumIdle(5);
        hikariConfig.setDriverClassName("org.sqlite.JDBC");
        this.dataSource = new HikariDataSource(hikariConfig);
    }

    public Connection getConnection() throws SQLException {
        return this.dataSource.getConnection();
    }
}
