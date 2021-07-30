package com.github.ericliucn.crafteco.handler;

import com.github.ericliucn.crafteco.config.ConfigLoader;
import com.github.ericliucn.crafteco.config.CraftEcoConfig;
import com.github.ericliucn.crafteco.eco.account.CraftAccount;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
    public void createDB() {
    }

    @Override
    public void createTable() {
        String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + dbConfig.tableName + " (`uuid` VARCHAR (40) PRIMARY KEY, `data` BLOB)";
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(CREATE_TABLE)
                ){
            statement.execute();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    @Override
    public Optional<CraftAccount> getAccount(UUID uuid) {
        String GET_ACCOUNT = "SELECT `data` FROM " + dbConfig.tableName + " WHERE `uuid` = ?";
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(GET_ACCOUNT)
        ){
            statement.setString(1, uuid.toString());
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()){
                return Optional.ofNullable(CraftAccount.deserialize(resultSet.getBytes("data")));
            }
        }catch (SQLException | IOException | ClassNotFoundException e){
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public boolean createAccount(UUID uuid) {
        String CREATE_ACCOUNT = "INSERT INTO " + dbConfig.tableName + " (`uuid`, `data`) VALUES (?, ?)";
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

    @Override
    public boolean deleteAccount(UUID uuid) {
        String DELETE_ACCOUNT = "DELETE FROM " + dbConfig.tableName + " WHERE `uuid` = ?";
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

    @Override
    public boolean saveAccount(CraftAccount account) {
        String UPDATE_ACCOUNT = "UPDATE " + dbConfig.tableName + " SET `data` = ? WHERE `uuid` = ?";
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

    @Override
    public List<CraftAccount> getAllAccount() {
        List<CraftAccount> craftAccounts = new ArrayList<>();
        String GET_ALL_ACCOUNT = "SELECT `data` FROM " + dbConfig.tableName;
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(GET_ALL_ACCOUNT)
        ){
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()){
                byte[] bytes = resultSet.getBytes("data");
                craftAccounts.add(CraftAccount.deserialize(bytes));
            }
        }catch (SQLException | IOException | ClassNotFoundException e){
            e.printStackTrace();
        }
        return craftAccounts;
    }
}
