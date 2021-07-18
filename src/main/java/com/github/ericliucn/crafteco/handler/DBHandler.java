package com.github.ericliucn.crafteco.handler;

import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.account.UniqueAccount;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface DBHandler {

    String getFullTableName();

    void createDB();

    void createTable();

    BigDecimal getBalance(ServerPlayer player, Currency currency);

    BigDecimal getBalance(ServerPlayer player);

    void setBalance(ServerPlayer player, Currency currency, BigDecimal value);

    void setBalance(ServerPlayer player, BigDecimal value);

    void addCurrency(Currency currency, BigDecimal defaultValue);

    List<String> getCurrencies();

    void createAccount(ServerPlayer player);

    Optional<UniqueAccount> getUniqueAccount(ServerPlayer player);


}
