package com.github.ericliucn.crafteco.config;

import com.github.ericliucn.crafteco.eco.CraftCurrency;
import net.kyori.adventure.text.Component;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.ArrayList;
import java.util.List;

@ConfigSerializable
public class CraftEcoConfig {

    @Comment("Database setting")
    public DatabaseConfig database;

    public CraftEcoConfig(){
        database = new DatabaseConfig();
    }

    @Setting(value = "Currencies")
    @Comment(value = "Add currency here, beware that only one currency be set default currency\n" +
            "The first currency with \"isDefault = true\" will be the default currency")
    public List<CraftCurrency> currencies = new ArrayList<CraftCurrency>(){{
        add(new CraftCurrency(Component.text("dollar"), Component.text("dollars"), Component.text("$"), true));
    }};

    @ConfigSerializable
    public static class DatabaseConfig {

        @Setting(value = "Database Type")
        public String dbType = "sqlite";

        @Setting(value = "root")
        public String username = "root";

        @Setting(value = "password")
        public String passwd = "932065";

        @Setting(value = "address")
        public String address = "127.0.0.1";

        @Setting(value = "port")
        public String port = "3389";

        @Setting(value = "database name")
        public String databaseName = "crafteco";

        @Setting(value = "table name")
        public String tableName = "main_data";

    }
}
