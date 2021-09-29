package com.github.ericliucn.crafteco.handler;

import com.github.ericliucn.crafteco.Main;
import com.github.ericliucn.crafteco.config.ConfigLoader;
import com.github.ericliucn.crafteco.config.CraftEcoConfig;
import com.github.ericliucn.crafteco.eco.account.CraftAccount;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.sql.SqlManager;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class DatabaseHandler {

    public static DatabaseHandler instance;
    private final CraftEcoConfig.DatabaseConfig config;
    private final DataSource dataSource;

    public DatabaseHandler() throws SQLException {
        config = ConfigLoader.instance.getConfig().database;
        String dbType = config.dbType;
        String jdbcURL = this.createJDBCURL(dbType);
        SqlManager sqlManager = Sponge.game().sqlManager();
        System.out.println(jdbcURL);
        dataSource = sqlManager.dataSource(jdbcURL);
        createDB();
        createTable();
    }

    private String createJDBCURL(String dbType){
        if (dbType.equalsIgnoreCase("mysql")){
            return "jdbc:mysql://" + config.address + ":" + config.port + "?user=" + config.username
                    + "&password=" + config.passwd;
        }
        return "jdbc:h2:" + Main.instance.getConfigDir().resolve(config.databaseName + ".db");
    }
    
    
    public void createDB() {
        if (config.dbType.equalsIgnoreCase("mysql")) {
            String sql = "CREATE DATABASE " + config.databaseName;
            try (
                    Connection connection = dataSource.getConnection();
                    PreparedStatement statement = connection.prepareStatement(sql)
            ) {
                statement.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    public void createTable() {
        String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + config.tableName + " (`uuid` VARCHAR (40) PRIMARY KEY, `data` BLOB)";
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(CREATE_TABLE)
        ){
            statement.execute();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }
    
    public Optional<CraftAccount> getAccount(UUID uuid) {
        String GET_ACCOUNT = "SELECT `data` FROM " + config.tableName + " WHERE `uuid` = ?";
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(GET_ACCOUNT)
        ){
            statement.setString(1, uuid.toString());
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()){
                return Optional.ofNullable(CraftAccount.deserialize(resultSet.getBytes("data")));
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
        return Optional.empty();
    }
    
    public boolean createAccount(UUID uuid) {
        String CREATE_ACCOUNT = "INSERT INTO " + config.tableName + " (`uuid`, `data`) VALUES (?, ?)";
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(CREATE_ACCOUNT)
        ){
            statement.setString(1, uuid.toString());
            statement.setBytes(2, null);
            statement.execute();
            return true;
        }catch (SQLException e){
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean deleteAccount(UUID uuid) {
        String DELETE_ACCOUNT = "DELETE FROM " + config.tableName + " WHERE `uuid` = ?";
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(DELETE_ACCOUNT)
        ){
            statement.setString(1, uuid.toString());
            statement.execute();
            return true;
        }catch (SQLException e){
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean saveAccount(CraftAccount account) {
        String UPDATE_ACCOUNT = "UPDATE " + config.tableName + " SET `data` = ? WHERE `uuid` = ?";
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(UPDATE_ACCOUNT)
        ){
            statement.setBytes(1, account.serialize());
            statement.setString(2, account.identifier());
            statement.execute();
            return true;
        }catch (SQLException | IOException e){
            e.printStackTrace();
            return false;
        }
    }
    
    public List<CraftAccount> getAllAccount() {
        List<CraftAccount> craftAccounts = new ArrayList<>();
        String GET_ALL_ACCOUNT = "SELECT `data` FROM " + config.tableName;
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(GET_ALL_ACCOUNT)
        ){
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()){
                byte[] bytes = resultSet.getBytes("data");
                craftAccounts.add(CraftAccount.deserialize(bytes));
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
        return craftAccounts;
    }

}
