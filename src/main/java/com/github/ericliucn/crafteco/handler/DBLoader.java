package com.github.ericliucn.crafteco.handler;

import com.github.ericliucn.crafteco.config.ConfigLoader;
import com.github.ericliucn.crafteco.config.CraftEcoConfig;

import java.io.IOException;
import java.nio.file.Path;

public class DBLoader {

    private DBHandler dbHandler;

    public DBLoader(final Path configDir) throws IOException {
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
