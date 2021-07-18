package com.github.ericliucn.crafteco.handler;

import com.github.ericliucn.crafteco.Main;
import com.github.ericliucn.crafteco.config.ConfigLoader;
import com.github.ericliucn.crafteco.config.CraftEcoConfig;
import com.github.ericliucn.crafteco.utils.ComponentUtil;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.service.economy.Currency;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class SqliteHandler implements DBHandler{

    private DataSource dataSource;
    private CraftEcoConfig.DatabaseConfig dbConfig;

    public SqliteHandler(final Path configDir) throws IOException {
        final Path dbPath = configDir.resolve("data.db");
        if (!Files.exists(dbPath)){
            Files.createDirectory(dbPath);
        }
        dbConfig = ConfigLoader.instance.getConfig().database;
        dataSource = new DataSource("jdbc:sqlite:" + dbPath.toAbsolutePath(), dbConfig.username, dbConfig.passwd);
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
                PreparedStatement statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS" + getFullTableName() +
                        "uuid VARCHAR (40) NOT NULL ," +
                        "player_name VARCHAR (40)" +
                        "PRIMARY KEY(uuid)")
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
                PreparedStatement statement = connection.prepareStatement("SELECT ? FROM " + getFullTableName()
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
                PreparedStatement statement = connection.prepareStatement("SELECT ? FROM " + getFullTableName()
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
                PreparedStatement statement = connection.prepareStatement("UPDATE " + getFullTableName()
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
                PreparedStatement statement = connection.prepareStatement("UPDATE " + getFullTableName()
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
                PreparedStatement statement = connection.prepareStatement("ALTER TABLE " + getFullTableName() + " ADD COLUMN ? DECIMAL (18,5) DEFAULT ?")
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
        List<>
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement("ALTER TABLE " + getFullTableName() + " ADD COLUMN ? DECIMAL (18,5) DEFAULT ?")
        ){
            statement.setString(1, ComponentUtil.toPlain(currency.displayName()));
            statement.setBigDecimal(2, BigDecimal.ZERO);
            statement.execute();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }
}
