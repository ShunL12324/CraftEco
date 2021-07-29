package com.github.ericliucn.crafteco.eco;

import com.github.ericliucn.crafteco.utils.Util;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataSerializable;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.account.Account;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.service.economy.transaction.TransactionResult;
import org.spongepowered.api.service.economy.transaction.TransactionTypes;
import org.spongepowered.api.service.economy.transaction.TransferResult;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class CraftAccount implements Account, DataSerializable {

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
            return CraftEcoResults.builder().currency((CraftCurrency) currency).account(this).amount(amount).result(ResultType.SUCCESS).build();
        }else {
            throw new IllegalStateException("The currency must be CraftCurrency!");
        }
    }

    @Override
    public TransactionResult setBalance(Currency currency, BigDecimal amount, Set<Context> contexts) {
        return setBalance(currency, amount);
    }

    @Override
    public TransactionResult setBalance(Currency currency, BigDecimal amount, Cause cause) {
        return setBalance(currency, amount);
    }

    @Override
    public Map<Currency, TransactionResult> resetBalances() {
        final Map<Currency, TransactionResult> resultMap = new HashMap<>();
        for (Map.Entry<Currency, BigDecimal> entry : this.balanceMap.entrySet()) {
            resultMap.put(entry.getKey(), setBalance(entry.getKey(), defaultBalance(entry.getKey())));
        }
        return resultMap;
    }

    @Override
    public Map<Currency, TransactionResult> resetBalances(Set<Context> contexts) {
        return resetBalances();
    }

    @Override
    public Map<Currency, TransactionResult> resetBalances(Cause cause) {
        return resetBalances();
    }

    @Override
    public TransactionResult resetBalance(Currency currency) {
        return setBalance(currency, defaultBalance(currency));
    }

    @Override
    public TransactionResult resetBalance(Currency currency, Set<Context> contexts) {
        return resetBalance(currency);
    }

    @Override
    public TransactionResult resetBalance(Currency currency, Cause cause) {
        return resetBalance(currency);
    }

    @Override
    public TransactionResult deposit(Currency currency, BigDecimal amount) {
        if (currency instanceof CraftCurrency){
            this.balanceMap.put(currency, balance(currency).add(amount));
            return CraftEcoResults.builder()
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
        return deposit(currency, amount);
    }

    @Override
    public TransactionResult deposit(Currency currency, BigDecimal amount, Cause cause) {
        return deposit(currency, amount);
    }

    @Override
    public TransactionResult withdraw(Currency currency, BigDecimal amount) {
        if (currency instanceof CraftCurrency){
            this.balanceMap.put(currency, balance(currency).subtract(amount));
            return CraftEcoResults.builder()
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
        return withdraw(currency, amount);
    }

    @Override
    public TransactionResult withdraw(Currency currency, BigDecimal amount, Cause cause) {
        return withdraw(currency, amount);
    }

    @Override
    public TransferResult transfer(Account to, Currency currency, BigDecimal amount) {
        if ((to instanceof CraftAccount) && (currency instanceof CraftCurrency)){
            try {
                withdraw(currency, amount);
                to.deposit(currency, amount);
                return TransferResult
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public TransferResult transfer(Account to, Currency currency, BigDecimal amount, Set<Context> contexts) {
        return transfer(to, currency, amount);
    }

    @Override
    public TransferResult transfer(Account to, Currency currency, BigDecimal amount, Cause cause) {
        return transfer(to, currency, amount);
    }


    @Override
    public String identifier() {
        return this.identifier;
    }

    @Override
    public int contentVersion() {
        return 0;
    }

    @Override
    public DataContainer toContainer() {
        return null;
    }
}
