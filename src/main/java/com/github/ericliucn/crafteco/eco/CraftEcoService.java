package com.github.ericliucn.crafteco.eco;

import com.github.ericliucn.crafteco.Main;
import com.github.ericliucn.crafteco.config.ConfigLoader;
import com.github.ericliucn.crafteco.config.CraftEcoConfig;
import com.github.ericliucn.crafteco.eco.account.CraftAccount;
import com.github.ericliucn.crafteco.eco.account.CraftVirtualAccount;
import com.github.ericliucn.crafteco.handler.DatabaseHandler;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.scheduler.TaskExecutorService;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.account.*;
import org.spongepowered.api.util.Nameable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CraftEcoService implements EconomyService {

    public static CraftEcoService instance;

    private final CraftEcoConfig config;
    private final Map<UUID, CraftAccount> accounts = new ConcurrentHashMap<>();
    private final TaskExecutorService executorService = Sponge.asyncScheduler().executor(Main.instance.getContainer());

    public CraftEcoService(){
        instance = this;
        config = ConfigLoader.instance.getConfig();
        loadCache();
    }

    public void loadCache(){
        accounts.clear();
        DatabaseHandler.instance.allAccounts().forEach(acc -> {
            config.currencies.forEach(cur -> {
                if (!acc.hasBalance(cur)) acc.resetBalance(cur);
            });
            accounts.put(acc.uniqueId(), acc);
        });
    }

    public void saveCache(){
        for (CraftAccount value : this.accounts.values()) {
            DatabaseHandler.instance.saveAccount(value);
        }
    }

    @Override
    public Currency defaultCurrency() {
        return config.currencies.stream()
                .filter(CraftCurrency::isDefault)
                .findFirst()
                .get();
    }

    @Override
    public boolean hasAccount(UUID uuid) {
        return accounts.containsKey(uuid);
    }

    @Override
    public boolean hasAccount(String identifier) {
        return accounts.values().stream()
                .map(CraftAccount::identifier)
                .anyMatch(s -> s.equalsIgnoreCase(identifier));
    }

    @Override
    public Optional<UniqueAccount> findOrCreateAccount(UUID uuid) {
        if (!hasAccount(uuid)){
            // we are going to create an account
            try {
                DatabaseHandler.instance.createAccount(uuid);
                CraftAccount account = new CraftAccount(uuid.toString(), uuid);
                account.resetBalances();
                DatabaseHandler.instance.saveAccount(account);
                this.accounts.put(uuid, account);
                executorService.submit(() -> {
                    String identifier = Sponge.server().onlinePlayers().stream()
                            .filter(serverPlayer -> serverPlayer.uniqueId().equals(uuid))
                            .findFirst()
                            .map(Nameable::name)
                            .orElse(null);
                    if (identifier == null && Sponge.server().userManager().exists(uuid)){
                        Optional<User> optionalUser = Sponge.server().userManager().load(uuid)
                                .getNow(Optional.empty());
                        if (optionalUser.isPresent()){
                            identifier = optionalUser.get().name();
                        }
                    }
                    if (identifier != null){
                        account.setIdentifier(identifier);
                        DatabaseHandler.instance.saveAccount(account);
                    }
                });
                return Optional.of(account);
            }catch (Exception e){
                Main.instance.logger().error("Failed create account for {}", uuid);
                e.printStackTrace();
                return Optional.empty();
            }
        }else {
            return Optional.of(accounts.get(uuid));
        }
    }

    @Override
    public Optional<Account> findOrCreateAccount(String identifier) {
        if (!hasAccount(identifier)){
            try {
                UUID uuid = UUID.randomUUID();
                DatabaseHandler.instance.createAccount(uuid);
                CraftAccount account = new CraftAccount(identifier, uuid);
                account.setIdentifier(identifier);
                account.resetBalances();
                DatabaseHandler.instance.saveAccount(account);
                this.accounts.put(uuid, account);
                return Optional.of(account);
            }catch (Exception e){
                Main.instance.logger().error("Failed to create account {}", identifier);
                e.printStackTrace();
                return Optional.empty();
            }
        }else {
            return accounts.values().stream()
                    .filter(acc -> acc.identifier().equalsIgnoreCase(identifier))
                    .map(acc -> ((Account) acc))
                    .findFirst();
        }
    }

    @Override
    public Stream<UniqueAccount> streamUniqueAccounts() {
        return accounts.values().stream().filter(acc -> !acc.isVirtual()).map(acc -> acc);
    }

    @Override
    public Collection<UniqueAccount> uniqueAccounts() {
        return streamUniqueAccounts().collect(Collectors.toList());
    }

    @Override
    public Stream<VirtualAccount> streamVirtualAccounts() {
        return accounts.values().stream().filter(CraftAccount::isVirtual).map(acc -> ((CraftVirtualAccount) acc));
    }

    @Override
    public Collection<VirtualAccount> virtualAccounts() {
        return streamVirtualAccounts().collect(Collectors.toList());
    }

    @Override
    public AccountDeletionResultType deleteAccount(UUID uuid) {
        try {
            this.accounts.remove(uuid);
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
            accounts.values().stream()
                    .filter(acc -> acc.identifier().equals(identifier))
                    .forEach(acc -> {
                        if (acc.identifier().equals(identifier)){
                            accounts.remove(acc.uniqueId());
                            deleteAccount(acc.uniqueId());
                        }
                    });
            return AccountDeletionResultTypes.SUCCESS.get();
        }catch (Exception e){
            e.printStackTrace();
            return AccountDeletionResultTypes.FAILED.get();
        }
    }

    public Optional<Currency> currency(String name){
        return ConfigLoader.instance.getConfig().currencies.stream()
                .filter(cur -> cur.toPlain().equalsIgnoreCase(name)).map(craftCurrency -> ((Currency) craftCurrency))
                .findFirst();
    }

    public List<Currency> currencies(){
        return ConfigLoader.instance.getConfig().currencies.stream()
                .map(cur -> ((Currency) cur))
                .collect(Collectors.toList());
    }


}
