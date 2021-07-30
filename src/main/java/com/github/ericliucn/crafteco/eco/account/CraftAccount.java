package com.github.ericliucn.crafteco.eco;

import com.github.ericliucn.crafteco.utils.Util;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.account.Account;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.service.economy.transaction.TransactionResult;
import org.spongepowered.api.service.economy.transaction.TransactionTypes;
import org.spongepowered.api.service.economy.transaction.TransferResult;

import java.io.*;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CraftAccount implements Account {

    private final Map<Currency, BigDecimal> balanceMap;
    private final String identifier;

    public CraftAccount(final Map<Currency, BigDecimal> balanceMap, final String identifier){
        this.balanceMap = balanceMap;
        this.identifier = identifier;
    }

    public CraftAccount(final String identifier){
        this.balanceMap = new HashMap<>();
        this.identifier = identifier;
    }

    @Override
    public Component displayName() {
        return Util.toComponent(this.identifier);
    }

    @Override
    public BigDecimal defaultBalance(Currency currency) {
        return currency instanceof CraftCurrency ? ((CraftCurrency) currency).defaultValue():BigDecimal.ZERO;
    }

    @Override
    public boolean hasBalance(Currency currency, Set<Context> contexts) {
        return balanceMap.containsKey(currency);
    }

    @Override
    public boolean hasBalance(Currency currency, Cause cause) {
        return balanceMap.containsKey(currency);
    }

    @Override
    public BigDecimal balance(Currency currency, Set<Context> contexts) {
        return balanceMap.getOrDefault(currency, defaultBalance(currency));
    }

    @Override
    public BigDecimal balance(Currency currency, Cause cause) {
        return balanceMap.getOrDefault(currency, defaultBalance(currency));
    }

    @Override
    public Map<Currency, BigDecimal> balances(Set<Context> contexts) {
        return balances();
    }

    @Override
    public Map<Currency, BigDecimal> balances(Cause cause) {
        return balances();
    }

    @Override
    public TransactionResult setBalance(Currency currency, BigDecimal amount) {
        if (currency instanceof CraftCurrency){
            this.balanceMap.put(currency, amount);
            return CraftResult.builder().currency((CraftCurrency) currency).account(this).amount(amount).result(ResultType.SUCCESS).build();
        }else {
            throw new IllegalStateException("The currency must be CraftCurrency!");
        }
    }

    @Override
    public TransactionResult setBalance(Currency currency, BigDecimal amount, Set<Context> contexts) {
        return ((CraftResult) setBalance(currency, amount)).toBuilder().contexts(contexts).build();
    }

    @Override
    public TransactionResult setBalance(Currency currency, BigDecimal amount, Cause cause) {
        return ((CraftResult) setBalance(currency, amount)).toBuilder().cause(cause).build();
    }

    @Override
    public Map<Currency, TransactionResult> resetBalances() {
        final Map<Currency, TransactionResult> resultMap = new HashMap<>();
        for (Map.Entry<Currency, BigDecimal> entry : this.balanceMap.entrySet()) {
            CraftCurrency craftCurrency = (CraftCurrency) entry.getKey();
            resultMap.put(craftCurrency, setBalance(craftCurrency, defaultBalance(craftCurrency)));
        }
        return resultMap;
    }

    @Override
    public Map<Currency, TransactionResult> resetBalances(Set<Context> contexts) {
        Map<Currency, TransactionResult> map = resetBalances();
        map.replaceAll((k, v) -> ((CraftResult) v).toBuilder().contexts(contexts).build());
        return map;
    }

    @Override
    public Map<Currency, TransactionResult> resetBalances(Cause cause) {
        Map<Currency, TransactionResult> map = resetBalances();
        map.replaceAll((k, v) -> ((CraftResult) v).toBuilder().cause(cause).build());
        return map;
    }

    @Override
    public TransactionResult resetBalance(Currency currency) {
        return setBalance(currency, defaultBalance(currency));
    }

    @Override
    public TransactionResult resetBalance(Currency currency, Set<Context> contexts) {
        return ((CraftResult) resetBalance(currency)).toBuilder().contexts(contexts).build();
    }

    @Override
    public TransactionResult resetBalance(Currency currency, Cause cause) {
        return ((CraftResult) resetBalance(currency)).toBuilder().cause(cause).build();
    }

    @Override
    public TransactionResult deposit(Currency currency, BigDecimal amount) {
        if (currency instanceof CraftCurrency){
            this.balanceMap.put(currency, balance(currency).add(amount));
            return CraftResult.builder()
                    .currency((CraftCurrency) currency)
                    .account(this)
                    .result(ResultType.SUCCESS)
                    .type(TransactionTypes.DEPOSIT.get())
                    .amount(amount)
                    .build();
        }else {
            throw new IllegalStateException("The currency must be CraftCurrency!");
        }
    }

    @Override
    public TransactionResult deposit(Currency currency, BigDecimal amount, Set<Context> contexts) {
        return ((CraftResult) deposit(currency, amount)).toBuilder().contexts(contexts).build();
    }

    @Override
    public TransactionResult deposit(Currency currency, BigDecimal amount, Cause cause) {
        return ((CraftResult) deposit(currency, amount)).toBuilder().cause(cause).build();
    }

    @Override
    public TransactionResult withdraw(Currency currency, BigDecimal amount) {
        if (currency instanceof CraftCurrency){
            this.balanceMap.put(currency, balance(currency).subtract(amount));
            return CraftResult.builder()
                    .currency((CraftCurrency) currency)
                    .account(this)
                    .result(ResultType.SUCCESS)
                    .type(TransactionTypes.WITHDRAW.get())
                    .amount(amount)
                    .build();
        }else {
            throw new IllegalStateException("The currency must be CraftCurrency!");
        }
    }

    @Override
    public TransactionResult withdraw(Currency currency, BigDecimal amount, Set<Context> contexts) {
        return ((CraftResult) withdraw(currency, amount)).toBuilder().contexts(contexts).build();
    }

    @Override
    public TransactionResult withdraw(Currency currency, BigDecimal amount, Cause cause) {
        return ((CraftResult) withdraw(currency, amount)).toBuilder().cause(cause).build();
    }

    @Override
    public TransferResult transfer(Account to, Currency currency, BigDecimal amount) {
        if ((to instanceof CraftAccount) && (currency instanceof CraftCurrency)){
            CraftResult wResult = (CraftResult) withdraw(currency, amount);
            CraftResult dResult = (CraftResult) to.deposit(currency, amount);
            if (wResult.result().equals(ResultType.SUCCESS) && dResult.result().equals(ResultType.SUCCESS)){
                return wResult.toBuilder().type(TransactionTypes.TRANSFER.get()).build().toTransferResult(((CraftAccount) to));
            }else {
                throw new IllegalStateException("Transfer failed!");
            }
        }else {
            throw new IllegalStateException("The currency must be CraftCurrency!");
        }
    }

    @Override
    public TransferResult transfer(Account to, Currency currency, BigDecimal amount, Set<Context> contexts) {
        return ((CraftResult.CraftTransferResult) transfer(to, currency, amount)).toBuilder().contexts(contexts).build().toTransferResult(((CraftAccount) to));
    }

    @Override
    public TransferResult transfer(Account to, Currency currency, BigDecimal amount, Cause cause) {
        return ((CraftResult.CraftTransferResult) transfer(to, currency, amount)).toBuilder().cause(cause).build().toTransferResult(((CraftAccount) to));
    }


    @Override
    public String identifier() {
        return this.identifier;
    }

    public byte[] serialize() throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(this);
        return byteArrayOutputStream.toByteArray();
    }

    public static CraftAccount deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
        return ((CraftAccount) objectInputStream.readObject());
    }

}
