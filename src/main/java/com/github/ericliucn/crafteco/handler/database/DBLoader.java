package com.github.ericliucn.crafteco.handler.database;

import com.github.ericliucn.crafteco.config.ConfigLoader;
import com.github.ericliucn.crafteco.config.CraftEcoConfig;

import java.io.IOException;
import java.nio.file.Path;

public class DBLoader {

    public static DBLoader instance;
    private final DBHandler dbHandler;

    public DBLoader(final Path configDir) throws IOException {
        instance = this;
        CraftEcoConfig.DatabaseConfig config = ConfigLoader.instance.getConfig().database;
        switch (config.dbType){
            case "sqlite":
                dbHandler = new SqliteHandler(configDir);
                break;
            default:
                dbHandler = new SqliteHandler(configDir);
        }
    }

    public DBHandler getDbHandler() {
        return dbHandler;
    }
}
