package com.github.ericliucn.crafteco.eco;

import com.github.ericliucn.crafteco.Main;
import com.github.ericliucn.crafteco.utils.ComponentUtil;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.data.SerializableDataHolder;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.account.Account;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.service.economy.transaction.TransactionResult;
import org.spongepowered.api.service.economy.transaction.TransactionTypes;
import org.spongepowered.api.service.economy.transaction.TransferResult;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class CraftAccount implements UniqueAccount {

    private Map<Currency, BigDecimal> balance = new HashMap<>();
    private final UUID userUUID;
    private final boolean isUnique;

    public CraftAccount(UUID uuid, Map<Currency, BigDecimal> balance, boolean isUnique){
        this.userUUID = uuid;
        this.balance = balance;
        this.isUnique = isUnique;
    }

    public CraftAccount(UUID uuid, boolean isUnique){
        this.userUUID = uuid;
        this.isUnique = isUnique;
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
            return CraftEcoResults.DEPOSIT_FAIL().account(this).contexts(contexts).currency(currency).amount(amount).build();
        }
        return CraftEcoResults.DEPOSIT_SUCCESS().account(this).contexts(contexts).currency(currency).amount(amount).build();
    }

    @Override
    public TransactionResult setBalance(Currency currency, BigDecimal amount, Cause cause) {
        if ((currency instanceof CraftCurrency)){
            balance.put(currency, amount);
            return CraftEcoResults.DEPOSIT_SUCCESS().account(this).contexts(toContexts(cause)).currency(currency).amount(amount).build();
        }
        return CraftEcoResults.DEPOSIT_FAIL().account(this).contexts(toContexts(cause)).currency(currency).amount(amount).build();
    }

    @Override
    public Map<Currency, TransactionResult> resetBalances(Set<Context> contexts) {
        Map<Currency, TransactionResult> resultMap = new HashMap<>();
        balance.forEach((key, value) ->
                resultMap.put(key, setBalance(key, ((CraftCurrency) key).defaultValue(), contexts)
        ));
        return resultMap;
    }

    @Override
    public Map<Currency, TransactionResult> resetBalances(Cause cause) {
        Map<Currency, TransactionResult> resultMap = new HashMap<>();
        balance.forEach((key, value) ->
                resultMap.put(key, setBalance(key, ((CraftCurrency) key).defaultValue(), cause)
                ));
        return resultMap;
    }

    @Override
    public TransactionResult resetBalance(Currency currency, Set<Context> contexts) {
        return setBalance(currency, ((CraftCurrency) currency).defaultValue(), contexts);
    }

    @Override
    public TransactionResult resetBalance(Currency currency, Cause cause) {
        return setBalance(currency, ((CraftCurrency) currency).defaultValue(), cause);
    }

    @Override
    public TransactionResult deposit(Currency currency, BigDecimal amount, Set<Context> contexts) {
        return setBalance(currency, balance(currency, contexts).add(amount), contexts);
    }

    @Override
    public TransactionResult deposit(Currency currency, BigDecimal amount, Cause cause) {
        return setBalance(currency, balance(currency, cause).add(amount), cause);
    }

    @Override
    public TransactionResult withdraw(Currency currency, BigDecimal amount, Set<Context> contexts) {
        return setBalance(currency, balance(currency, contexts).subtract(amount), contexts);
    }

    @Override
    public TransactionResult withdraw(Currency currency, BigDecimal amount, Cause cause) {
        return setBalance(currency, balance(currency, cause).subtract(amount), cause);
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
        return this.userUUID.toString();
    }

    @Override
    public UUID uniqueId() {
        return this.userUUID;
    }

    private Set<Context> toContexts(Cause cause){
        return cause.all().stream().map(o -> ((Context) o)).collect(Collectors.toSet());
    }

    public ByteArrayOutputStream serialize() throws IOException {
        DataContainer container = DataContainer.createNew();
        container.set(DataQuery.of("balance"), this.balance);
        container.set(DataQuery.of("uuid"), this.userUUID.toString());
        container.set(DataQuery.of("isUnique"), this.isUnique);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        DataFormats.NBT.get().writeTo(stream, container);
        return stream;
    }

    public static Optional<CraftAccount> fromContainer(final DataContainer container){
        try {
            Map<Currency, BigDecimal> map = ((Map<Currency, BigDecimal>) container.getMap(DataQuery.of("balance")).get());
            UUID uuid = UUID.fromString(container.getString(DataQuery.of("uuid")).get());
            boolean isUnique = container.getBoolean(DataQuery.of("isUnique")).get();
            return Optional.of(new CraftAccount(uuid, map, isUnique));
        }catch (Exception e){
            e.printStackTrace();
            return Optional.empty();
        }
    }

}
