package com.github.ericliucn.crafteco.config;

import com.github.ericliucn.crafteco.eco.CraftCurrency;
import net.kyori.adventure.text.Component;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@ConfigSerializable
public class CraftEcoConfig {

    @Comment("Database setting")
    public DatabaseConfig database;

    @Comment("Messages\n" +
            "")
    public Messages messages;

    public CraftEcoConfig(){
        database = new DatabaseConfig();
        messages = new Messages();
    }

    @Setting(value = "Currencies")
    @Comment(value = "Add currency here, beware that only one currency be set default currency\n" +
            "The first currency with \"isDefault = true\" will be the default currency")
    public List<CraftCurrency> currencies = new ArrayList<CraftCurrency>(){{
        add(new CraftCurrency(Component.text("dollar"), Component.text("dollars"), Component.text("$"), true, BigDecimal.ZERO));
    }};

    @ConfigSerializable
    public static class DatabaseConfig {

        @Comment("Possible value: sqlite/mysql")
        @Setting(value = "databaseType")
        public String dbType = "sqlite";

        @Setting(value = "username")
        public String username = "root";

        @Setting(value = "password")
        public String passwd = "932065";

        @Setting(value = "address")
        public String address = "127.0.0.1";

        @Setting(value = "port")
        public String port = "3389";

        @Setting(value = "databaseName")
        public String databaseName = "crafteco";

        @Setting(value = "tableName")
        public String tableName = "eco_data";

    }

    @ConfigSerializable
    public static class Messages{

        @Setting
        public String transfer_failed = "&a向 &4%transfer_target% &a支付 &e%cur_symbol% &4%amount% &a的交易失败了";

        @Setting
        public String transfer_failed_no_funds = "&a向 &4%transfer_target% &a支付 &e%cur_symbol% &4%amount% &a的交易失败了，\n" +
                "因为你没有足够的钱";

        @Setting
        public String transfer_success_receiver = "&a你收到了一笔来自 &4%transfer_source% &a的支付，金额为&e%cur_symbol% &4%amount%";

        @Setting
        public String transfer_success_payee = "&a你向 &4%transfer_target% &a支付了 &e%cur_symbol% &4%amount%";

        @Setting
        public String deposit_success_depositor = "&a成功向 %name% 的账户中存入了 &e%cur_symbol% &4%amount%&a，当前的余额为 " +
                "&e%cur_symbol% &4%balance%";

        @Setting
        public String deposit_success_receiver = "&a你的账户增加了 &e%cur_symbol% &4%amount%&a, 当前余额为 &e%cur_symbol% " +
                "&4%balance%";

        @Setting
        public String deposit_failed_depositor = "&4未能成功向目标玩家的账户中存入";

        @Setting
        public String command_need_user = "&4改指令在控制台执行需要指定玩家名";



    }
}
