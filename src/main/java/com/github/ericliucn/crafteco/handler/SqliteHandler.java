package com.github.ericliucn.crafteco.handler;

import com.github.ericliucn.crafteco.config.ConfigLoader;
import com.github.ericliucn.crafteco.config.CraftEcoConfig;
import com.github.ericliucn.crafteco.eco.CraftAccount;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataFormats;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

public class SqliteHandler implements DBHandler{

    private final DataSource dataSource;
    private final CraftEcoConfig.DatabaseConfig dbConfig;

    private static String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS ? (`uuid` VARCHAR (40) PRIMARY KEY, `data` BLOB)";
    private static String CREATE_ACCOUNT = "INSERT INTO TABLE ? (`uuid`, `data`) VALUES (?, ?)";
    private static String GET_ACCOUNT = "SELECT `data` FROM ? WHERE `uuid` = ?";
    private static String DELETE_ACCOUNT = "DELETE FROM ? WHERE `uuid` = ?";
    private static String UPDATE_ACCOUNT = "UPDATE ? SET `data` = ? WHERE `uuid` = ?";

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
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(CREATE_TABLE.replace("?", dbConfig.tableName))
                ){
            statement.execute();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    @Override
    public Optional<CraftAccount> getAccount(UUID uuid) {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(GET_ACCOUNT)
        ){
            statement.setString(1, dbConfig.tableName);
            statement.setString(2, uuid.toString());
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()){
                InputStream stream = resultSet.getBinaryStream(1);
                DataContainer container = DataFormats.NBT.get().readFrom(stream);
                return CraftAccount.fromContainer(container);
            }
        }catch (SQLException | IOException e){
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public boolean createAccount(UUID uuid) {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(CREATE_ACCOUNT)
        ){
            statement.setString(1, dbConfig.tableName);
            statement.setString(2, uuid.toString());
            statement.setBlob(3, new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8)));
            statement.execute();
        }catch (SQLException e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean deleteAccount(UUID uuid) {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(DELETE_ACCOUNT)
        ){
            statement.setString(1, dbConfig.tableName);
            statement.setString(2, uuid.toString());
            statement.execute();
        }catch (SQLException e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean saveAccount(CraftAccount account) {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(UPDATE_ACCOUNT)
        ){
            statement.setString(1, dbConfig.tableName);
            statement.setBinaryStream(2, new ByteArrayInputStream(account.serialize().toByteArray()));
            statement.setString(3, account.identifier());
            statement.execute();
        }catch (SQLException | IOException e){
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
