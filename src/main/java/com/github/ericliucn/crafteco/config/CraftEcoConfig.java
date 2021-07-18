package com.github.ericliucn.crafteco.config;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@ConfigSerializable
public class CraftEcoConfig {

    public DatabaseConfig database;

    public CraftEcoConfig(){
        database = new DatabaseConfig();
    }

    @ConfigSerializable
    public static class DatabaseConfig {

        @Setting(value = "root")
        public String username = "root";

        @Setting(value = "password")
        public String passwd = "932065";

        @Setting(value = "database name")
        public String databaseName = "crafteco";

        @Setting(value = "table name")
        public String tableName = "main_data";
    }
}
