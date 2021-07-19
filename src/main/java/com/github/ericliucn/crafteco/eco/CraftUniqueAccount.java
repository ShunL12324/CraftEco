package com.github.ericliucn.crafteco.eco;

import com.github.ericliucn.crafteco.Main;
import com.github.ericliucn.crafteco.handler.DBLoader;
import com.github.ericliucn.crafteco.utils.ComponentUtil;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.account.Account;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.service.economy.transaction.TransactionResult;
import org.spongepowered.api.service.economy.transaction.TransactionTypes;
import org.spongepowered.api.service.economy.transaction.TransferResult;

import java.math.BigDecimal;
import java.util.*;

public class CraftUniqueAccount implements UniqueAccount, CraftAccount{

    private Map<Currency, BigDecimal> balance = new HashMap<>();
    private final UUID userUUID;

    public CraftUniqueAccount(UUID uuid, Map<Currency, BigDecimal> balance){
        this.userUUID = uuid;
        this.balance = balance;
    }

    public CraftUniqueAccount(UUID uuid){
        this.userUUID = uuid;
        for (CraftCurrency currency : Main.instance.getCraftEcoService().currencies) {
            balance.put(currency, currency.defaultValue());
        }
    }

    @Override
    public Component displayName() {
        return ComponentUtil.toComponent("&a" + this.identifier());
    }

    @Override
    public BigDecimal defaultBalance(Currency currency) {
        if (currency instanceof CraftCurrency){
            return ((CraftCurrency) currency).defaultValue();
        }
        return BigDecimal.ZERO;
    }

    @Override
    public boolean hasBalance(Currency currency, Set<Context> contexts) {
        return balance.containsKey(currency);
    }

    @Override
    public boolean hasBalance(Currency currency, Cause cause) {
        return balance.containsKey(currency);
    }

    @Override
    public BigDecimal balance(Currency currency, Set<Context> contexts) {
        return balance.getOrDefault(currency, defaultBalance(currency));
    }

    @Override
    public BigDecimal balance(Currency currency, Cause cause) {
        return balance.getOrDefault(currency, defaultBalance(currency));
    }

    @Override
    public Map<Currency, BigDecimal> balances(Set<Context> contexts) {
        return this.balance;
    }

    @Override
    public Map<Currency, BigDecimal> balances(Cause cause) {
        return this.balance;
    }

    @Override
    public TransactionResult setBalance(Currency currency, BigDecimal amount, Set<Context> contexts) {
        if ((currency instanceof CraftCurrency) && balance.containsKey(currency)){
            balance.put(currency, amount);
            return new CraftEcoResults(this, currency, amount, contexts, TransactionTypes.DEPOSIT.get(), ResultType.SUCCESS);
        }
        return new CraftEcoResults(this, currency, amount, contexts, TransactionTypes.DEPOSIT.get(), ResultType.FAILED);
    }

    @Override
    public TransactionResult setBalance(Currency currency, BigDecimal amount, Cause cause) {
        if ((currency instanceof CraftCurrency) && balance.containsKey(currency)){
            balance.put(currency, amount);
            return new CraftEcoResults(this, currency, amount, new HashSet<Context>(){{add(((Context) cause.context()));}}, TransactionTypes.DEPOSIT.get(), ResultType.SUCCESS);
        }
        return new CraftEcoResults(this, currency, amount, contexts, TransactionTypes.DEPOSIT.get(), ResultType.FAILED);
    }

    @Override
    public Map<Currency, TransactionResult> resetBalances(Set<Context> contexts) {
        return null;
    }

    @Override
    public Map<Currency, TransactionResult> resetBalances(Cause cause) {
        return null;
    }

    @Override
    public TransactionResult resetBalance(Currency currency, Set<Context> contexts) {
        return null;
    }

    @Override
    public TransactionResult resetBalance(Currency currency, Cause cause) {
        return null;
    }

    @Override
    public TransactionResult deposit(Currency currency, BigDecimal amount, Set<Context> contexts) {
        return null;
    }

    @Override
    public TransactionResult deposit(Currency currency, BigDecimal amount, Cause cause) {
        return null;
    }

    @Override
    public TransactionResult withdraw(Currency currency, BigDecimal amount, Set<Context> contexts) {
        return null;
    }

    @Override
    public TransactionResult withdraw(Currency currency, BigDecimal amount, Cause cause) {
        return null;
    }

    @Override
    public TransferResult transfer(Account to, Currency currency, BigDecimal amount, Set<Context> contexts) {
        return null;
    }

    @Override
    public TransferResult transfer(Account to, Currency currency, BigDecimal amount, Cause cause) {
        return null;
    }

    @Override
    public String identifier() {
        return null;
    }

    @Override
    public UUID uniqueId() {
        return null;
    }
}
