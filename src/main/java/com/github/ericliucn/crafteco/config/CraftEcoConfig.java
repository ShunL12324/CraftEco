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
            "You can use these placeholders in the message: \n" +
            "%balance%, %cur_symbol%, %cur_name%, %amount%, %transfer_target%, %transfer_source%, %name%, %world%")
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

        @Comment("Possible value: h2/mysql")
        @Setting(value = "databaseType")
        public String dbType = "h2";

        @Setting(value = "username")
        public String username = "root";

        @Setting(value = "password")
        public String passwd = "password";

        @Setting(value = "address")
        public String address = "127.0.0.1";

        @Setting(value = "port")
        public String port = "3306";

        @Setting(value = "databaseName")
        @Comment(value = "you need create the database by your self, and set the database' name here")
        public String databaseName = "crafteco";

        @Setting(value = "tableName")
        @Comment(value = "the plugin will create the table automatically\n" +
                "change the table name will result the previous data lose")
        public String tableName = "eco_data";

    }

    @ConfigSerializable
    public static class Messages{

        @Setting
        public String transfer_failed = "&4Transfer failed";
                //"&a向 &4%transfer_target% &a支付 &e%cur_symbol% &4%amount% &a的交易失败了";

        @Setting
        public String transfer_failed_no_funds = "&4You don't have enough money, &dcurrent balance: &e%cur_symbol% &4%balance%";
                //"&a向 &4%transfer_target% &a支付 &e%cur_symbol% &4%amount% &a的交易失败了，\n" +
                //"因为你没有足够的钱";

        @Setting
        public String transfer_success_receiver = "&aPayment from &4%transfer_source% (&e%cur_symbol% &4%amount%) &areceived";
                //"&a你收到了一笔来自 &4%transfer_source% &a的支付，金额为&e%cur_symbol% &4%amount%";

        @Setting
        public String transfer_success_payee = "&aYou have paid &e%cur_symbol% &4%amount% &ato &4%transfer_target%";
                //"&a你向 &4%transfer_target% &a支付了 &e%cur_symbol% &4%amount%";

        @Setting
        public String deposit_success_depositor = "&adeposit success, current balance: &e%cur_symbol% &4%balance%";
                //"&a成功向 %name% 的账户中存入了 &e%cur_symbol% &4%amount%&a，当前的余额为 " +
                //"&e%cur_symbol% &4%balance%";

        @Setting
        public String deposit_success_receiver = "&aReceived &e%cur_symbol% &4%amount%&a, current balance &e%cur_symbol% &4%balance%";
                //"&a你的账户增加了 &e%cur_symbol% &4%amount%&a, 当前余额为 &e%cur_symbol% " +
                //"&4%balance%";

        @Setting
        public String deposit_failed_depositor = "&4deposite failed";
                //"&4未能成功向目标玩家的账户中存入";

        @Setting
        public String command_need_user = "&4need specific player name";
                //"&4改指令在控制台执行需要指定玩家名";

        @Setting
        public String eco_set_success = "&aOperation success, current balance: &e%cur_symbol% &4%balance%";
                //"&a成功设置了账户的金额，该账户当前对应货币余额为 &e%cur_symbol% &4%balance%";

        @Setting
        public String eco_set_failed = "&4Operation failed";
                //"&4未能成功设置该账户的金额，该账户当前余额为 &e%cur_symbol% &4%balance%";


    }
}
