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

    private final CraftEcoConfig config;
    private final Map<UUID, CraftAccount> uniqueAccounts = new ConcurrentHashMap<>();
    private final Map<String, CraftVirtualAccount> virtualAccounts = new ConcurrentHashMap<>();

    public CraftEcoService(){
        config = ConfigLoader.instance.getConfig();
        loadCache();
    }

    public void loadCache(){
        uniqueAccounts.clear();
        virtualAccounts.clear();
        for (CraftAccount craftAccount : DBLoader.instance.getDbHandler().getAllAccount()) {
            for (CraftCurrency currency : config.currencies) {
                if (!craftAccount.hasBalance(currency)){
                    craftAccount.resetBalance(currency);
                }
            }
            if (craftAccount instanceof CraftVirtualAccount){
                virtualAccounts.put(craftAccount.identifier(), (CraftVirtualAccount) craftAccount);
            }else {
                uniqueAccounts.put(craftAccount.uniqueId(), craftAccount);
            }
        }
    }

    public void saveCache(){
        for (CraftVirtualAccount value : this.virtualAccounts.values()) {
            DBLoader.instance.getDbHandler().saveAccount(value);
        }

        for (CraftAccount value : this.uniqueAccounts.values()) {
            DBLoader.instance.getDbHandler().saveAccount(value);
        }
    }

    @Override
    public Currency defaultCurrency() {
        return config.currencies.stream().filter(CraftCurrency::isDefault).findFirst().get();
    }

    @Override
    public boolean hasAccount(UUID uuid) {
        return uniqueAccounts.containsKey(uuid);
    }

    @Override
    public boolean hasAccount(String identifier) {
        return virtualAccounts.containsKey(identifier);
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
            this.uniqueAccounts.put(uuid, account);
            return Optional.of(account);
        }else {
            return Optional.ofNullable(uniqueAccounts.get(uuid));
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
            this.virtualAccounts.put(account.identifier(), account);
            return Optional.of(account);
        }else {
            return Optional.ofNullable(virtualAccounts.get(identifier));
        }
    }

    @Override
    public Stream<UniqueAccount> streamUniqueAccounts() {
        return uniqueAccounts.values().stream().map(craftAccount -> craftAccount);
    }

    @Override
    public Collection<UniqueAccount> uniqueAccounts() {
        return streamUniqueAccounts().collect(Collectors.toList());
    }

    @Override
    public Stream<VirtualAccount> streamVirtualAccounts() {
        return virtualAccounts.values().stream().map(account -> account);
    }

    @Override
    public Collection<VirtualAccount> virtualAccounts() {
        return streamVirtualAccounts().collect(Collectors.toList());
    }

    @Override
    public AccountDeletionResultType deleteAccount(UUID uuid) {
        try {
            this.uniqueAccounts.remove(uuid);
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
            DBLoader.instance.getDbHandler().deleteAccount(virtualAccounts.get(identifier).uniqueId());
            this.virtualAccounts.remove(identifier);
            return AccountDeletionResultTypes.SUCCESS.get();
        }catch (Exception e){
            e.printStackTrace();
            return AccountDeletionResultTypes.FAILED.get();
        }
    }

}
