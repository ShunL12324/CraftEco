package com.github.ericliucn.crafteco.eco;

import com.github.ericliucn.crafteco.config.ConfigLoader;
import com.github.ericliucn.crafteco.config.CraftEcoConfig;
import com.github.ericliucn.crafteco.eco.account.CraftAccount;
import com.github.ericliucn.crafteco.eco.account.CraftVirtualAccount;
import com.github.ericliucn.crafteco.handler.DatabaseHandler;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.account.*;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CraftEcoService implements EconomyService {

    public static CraftEcoService instance;

    private CraftEcoConfig config;
    private final Map<UUID, CraftAccount> allAccount = new ConcurrentHashMap<>();

    public CraftEcoService(){
        instance = this;
        config = ConfigLoader.instance.getConfig();
        loadCache();
    }

    public void loadCache(){
        allAccount.clear();
        config = ConfigLoader.instance.getConfig();
        for (CraftAccount craftAccount : DatabaseHandler.instance.getAllAccount()) {
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
            DatabaseHandler.instance.saveAccount(value);
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
            DatabaseHandler.instance.createAccount(uuid);
            CraftAccount account = new CraftAccount(uuid);
            for (CraftCurrency currency : config.currencies) {
                account.resetBalance(currency);
            }
            DatabaseHandler.instance.saveAccount(account);
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
            DatabaseHandler.instance.createAccount(uuid);
            CraftVirtualAccount account = new CraftVirtualAccount(identifier, uuid);
            for (CraftCurrency currency : config.currencies) {
                account.resetBalance(currency);
            }
            DatabaseHandler.instance.saveAccount(account);
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
            DatabaseHandler.instance.deleteAccount(uuid);
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

    public Optional<Currency> getCurrency(String name){
        return ConfigLoader.instance.getConfig().currencies.stream()
                .filter(cur -> cur.toPlain().equalsIgnoreCase(name)).map(craftCurrency -> ((Currency) craftCurrency))
                .findFirst();
    }

}
