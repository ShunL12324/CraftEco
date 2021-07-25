package com.github.ericliucn.crafteco.handler;

import com.github.ericliucn.crafteco.eco.CraftAccount;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.account.Account;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.economy.transaction.ResultType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DBHandler {

    void createDB();

    void createTable();

    Optional<CraftAccount> getAccount(UUID uuid);

    boolean createAccount(UUID uuid);

    boolean deleteAccount(UUID uuid);

    boolean saveAccount(CraftAccount account);


}
