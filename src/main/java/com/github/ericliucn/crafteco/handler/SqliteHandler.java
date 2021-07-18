package com.github.ericliucn.crafteco.handler;

import com.github.ericliucn.crafteco.Main;
import com.github.ericliucn.crafteco.config.ConfigLoader;
import com.github.ericliucn.crafteco.config.CraftEcoConfig;
import com.github.ericliucn.crafteco.utils.ComponentUtil;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.account.UniqueAccount;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqliteHandler implements DBHandler{

    private final DataSource dataSource;
    private final CraftEcoConfig.DatabaseConfig dbConfig;

    public SqliteHandler(final Path configDir) throws IOException {
        dbConfig = ConfigLoader.instance.getConfig().database;
        final Path dbPath = configDir.resolve(dbConfig.databaseName + ".db");
        if (!Files.exists(dbPath)){
            Files.createFile(dbPath);
        }
        dataSource = new DataSource("jdbc:sqlite:" + dbPath.toAbsolutePath(), dbConfig.username, dbConfig.passwd);
        this.createDB();
        this.createTable();
    }

    @Override
    public String getFullTableName() {
        return " " + dbConfig.databaseName + "." + dbConfig.tableName + " ";
    }

    @Override
    public void createDB() {
    }

    @Override
    public void createTable() {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + dbConfig.tableName +
                        "(uuid VARCHAR (40) PRIMARY KEY," +
                        "player_name VARCHAR (40))")
                ){
            statement.execute();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    @Override
    public BigDecimal getBalance(ServerPlayer player, Currency currency) {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement("SELECT ? FROM " + dbConfig.tableName
                        + " WHERE uuid = ?")
        ){
            statement.setString(1, ComponentUtil.toPlain(currency.displayName()));
            statement.setString(2, player.uniqueId().toString());
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()){
                return resultSet.getBigDecimal(1);
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getBalance(ServerPlayer player) {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement("SELECT ? FROM " + dbConfig.tableName
                        + " WHERE uuid = ?")
        ){
            statement.setString(1, ComponentUtil.toPlain(Main.instance.getCraftEcoService().defaultCurrency().displayName()));
            statement.setString(2, player.uniqueId().toString());
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()){
                return resultSet.getBigDecimal(1);
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
        return BigDecimal.ZERO;
    }

    @Override
    public void setBalance(ServerPlayer player, Currency currency,BigDecimal value) {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement("UPDATE " + dbConfig.tableName
                        + " SET ? = ?  WHERE uuid = ?")
        ){
            statement.setString(1, ComponentUtil.toPlain(currency.displayName()));
            statement.setBigDecimal(2, value);
            statement.setString(3, player.uniqueId().toString());
            statement.execute();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    @Override
    public void setBalance(ServerPlayer player, BigDecimal value) {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement("UPDATE " + dbConfig.tableName
                        + " SET ? = ?  WHERE uuid = ?")
        ){
            statement.setString(1, ComponentUtil.toPlain(Main.instance.getCraftEcoService().defaultCurrency().displayName()));
            statement.setBigDecimal(2, value);
            statement.setString(3, player.uniqueId().toString());
            statement.execute();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    @Override
    public void addCurrency(Currency currency, BigDecimal defaultValue) {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement("ALTER TABLE " + dbConfig.tableName + " ADD COLUMN ? DECIMAL (18,5) DEFAULT ?")
        ){
            statement.setString(1, ComponentUtil.toPlain(currency.displayName()));
            statement.setBigDecimal(2, BigDecimal.ZERO);
            statement.execute();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    @Override
    public List<String> getCurrencies() {
        List<String> list = new ArrayList<>();
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + dbConfig.tableName + " LIMIT 1")
        ){
            ResultSet resultSet = statement.executeQuery();
            ResultSetMetaData metaData = resultSet.getMetaData();
            for (int i = 0; i < metaData.getColumnCount(); i++) {
                list.add(metaData.getColumnName(i + 1));
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public void createAccount(ServerPlayer player) {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement("INSERT INTO " + dbConfig.tableName +
                        " (uuid, player_name) VALUES (?, ?)")
        ){
            statement.setString(1, player.uniqueId().toString());
            statement.setString(2, player.name());
            statement.execute()
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    @Override
    public Optional<UniqueAccount> getUniqueAccount(ServerPlayer player) {

        return Optional.empty();
    }
}
