package com.github.ericliucn.crafteco.eco;

import com.github.ericliucn.crafteco.config.ConfigLoader;
import com.github.ericliucn.crafteco.config.CraftEcoConfig;
import com.github.ericliucn.crafteco.eco.account.CraftAccount;
import com.github.ericliucn.crafteco.eco.account.CraftVirtualAccount;
import com.github.ericliucn.crafteco.handler.DBLoader;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.account.*;
import org.sqlite.core.DB;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CraftEcoService implements EconomyService {

    public static CraftEcoService instance;

    private final CraftEcoConfig config;
    private final Map<UUID, CraftAccount> allAccount = new ConcurrentHashMap<>();

    public CraftEcoService(){
        instance = this;
        config = ConfigLoader.instance.getConfig();
        loadCache();
    }

    public void loadCache(){
        allAccount.clear();
        for (CraftAccount craftAccount : DBLoader.instance.getDbHandler().getAllAccount()) {
            for (CraftCurrency currency : config.currencies) {
                if (!craftAccount.hasBalance(currency)){
                    craftAccount.resetBalance(currency);
                }
            }
            allAccount.put(craftAccount.uniqueId(), craftAccount);
        }
    }

    public void saveCache(){
        for (CraftAccount value : this.allAccount.values()) {
            DBLoader.instance.getDbHandler().saveAccount(value);
        }
    }

    @Override
    public Currency defaultCurrency() {
        return config.currencies.stream().filter(CraftCurrency::isDefault).findFirst().get();
    }

    @Override
    public boolean hasAccount(UUID uuid) {
        return allAccount.containsKey(uuid);
    }

    @Override
    public boolean hasAccount(String identifier) {
        return allAccount.values().stream().map(CraftAccount::identifier).anyMatch(s -> s.equals(identifier));
    }

    @Override
    public Optional<UniqueAccount> findOrCreateAccount(UUID uuid) {
        if (!hasAccount(uuid)){
            DBLoader.instance.getDbHandler().createAccount(uuid);
            CraftAccount account = new CraftAccount(uuid);
            for (CraftCurrency currency : config.currencies) {
                account.resetBalance(currency);
            }
            DBLoader.instance.getDbHandler().saveAccount(account);
            this.allAccount.put(uuid, account);
            return Optional.of(account);
        }else {
            return Optional.ofNullable(allAccount.get(uuid));
        }
    }

    @Override
    public Optional<Account> findOrCreateAccount(String identifier) {
        if (!hasAccount(identifier)){
            UUID uuid = UUID.randomUUID();
            DBLoader.instance.getDbHandler().createAccount(uuid);
            CraftVirtualAccount account = new CraftVirtualAccount(identifier, uuid);
            for (CraftCurrency currency : config.currencies) {
                account.resetBalance(currency);
            }
            DBLoader.instance.getDbHandler().saveAccount(account);
            this.allAccount.put(uuid, account);
            return Optional.of(account);
        }else {
            return allAccount.values().stream()
                    .filter(acc -> acc.identifier().equals(identifier))
                    .map(acc -> ((Account) acc))
                    .findFirst();
        }
    }

    @Override
    public Stream<UniqueAccount> streamUniqueAccounts() {
        return allAccount.values().stream().filter(acc -> !acc.isVirtual()).map(acc -> acc);
    }

    @Override
    public Collection<UniqueAccount> uniqueAccounts() {
        return streamUniqueAccounts().collect(Collectors.toList());
    }

    @Override
    public Stream<VirtualAccount> streamVirtualAccounts() {
        return allAccount.values().stream().filter(CraftAccount::isVirtual).map(acc -> ((CraftVirtualAccount) acc));
    }

    @Override
    public Collection<VirtualAccount> virtualAccounts() {
        return streamVirtualAccounts().collect(Collectors.toList());
    }

    @Override
    public AccountDeletionResultType deleteAccount(UUID uuid) {
        try {
            this.allAccount.remove(uuid);
            DBLoader.instance.getDbHandler().deleteAccount(uuid);
            return AccountDeletionResultTypes.SUCCESS.get();
        }catch (Exception e){
            e.printStackTrace();
            return AccountDeletionResultTypes.FAILED.get();
        }
    }

    @Override
    public AccountDeletionResultType deleteAccount(String identifier) {
        try {
            allAccount.values().stream()
                    .filter(acc -> acc.identifier().equals(identifier))
                    .forEach(acc -> {
                        if (acc.identifier().equals(identifier)){
                            allAccount.remove(acc.uniqueId());
                            deleteAccount(acc.uniqueId());
                        }
                    });
            return AccountDeletionResultTypes.SUCCESS.get();
        }catch (Exception e){
            e.printStackTrace();
            return AccountDeletionResultTypes.FAILED.get();
        }
    }

}
