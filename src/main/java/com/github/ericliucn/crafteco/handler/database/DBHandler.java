package com.github.ericliucn.crafteco.handler.database;

import com.github.ericliucn.crafteco.eco.account.CraftAccount;

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

    List<CraftAccount> getAllAccount();

}
