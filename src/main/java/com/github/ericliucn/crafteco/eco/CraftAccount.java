package com.github.ericliucn.crafteco.eco;

import com.github.ericliucn.crafteco.utils.ComponentUtil;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataSerializable;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.service.ban.Ban;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.account.Account;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.service.economy.transaction.TransactionResult;
import org.spongepowered.api.service.economy.transaction.TransferResult;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class CraftAccount implements Account, DataSerializable {

    private Map<Currency, BigDecimal> balanceMap = new HashMap<>();
    public CraftAccount(Map<Currency, BigDecimal> balanceMap){
        this.balanceMap = balanceMap;
    }

    public CraftAccount(){
    }

    @Override
    public Component displayName() {
        return Component.text(this.identifier());
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
        return this.balanceMap;
    }

    @Override
    public Map<Currency, BigDecimal> balances(Cause cause) {
        return this.balanceMap;
    }

    @Override
    public TransactionResult setBalance(Currency currency, BigDecimal amount, Set<Context> contexts) {
        if (!(currency instanceof CraftCurrency)) throw new IllegalStateException("The currency must be CraftCurrency");
        CraftCurrency craftCurrency = ((CraftCurrency) currency);
        try{
            this.balanceMap.put(currency, amount);
            return CraftEcoResults.builder().account(this).amount(amount).currency(craftCurrency).contexts(contexts).result(ResultType.SUCCESS).build();
        }catch (Exception e){
            return CraftEcoResults.builder().account(this).amount(amount).currency(craftCurrency).contexts(contexts).result(ResultType.SUCCESS).build();
        }
    }

    @Override
    public TransactionResult setBalance(Currency currency, BigDecimal amount, Cause cause) {
        if (!(currency instanceof CraftCurrency)) throw new IllegalStateException("The currency must be CraftCurrency");
        CraftCurrency craftCurrency = ((CraftCurrency) currency);
        try{
            this.balanceMap.put(currency, amount);
            return CraftEcoResults.builder().account(this).amount(amount).currency(craftCurrency).contexts(toContexts(cause)).result(ResultType.SUCCESS).build();
        }catch (Exception e){
            return CraftEcoResults.builder().account(this).amount(amount).currency(craftCurrency).contexts(toContexts(cause)).result(ResultType.SUCCESS).build();
        }
    }

    @Override
    public Map<Currency, TransactionResult> resetBalances(Set<Context> contexts) {
        final Map<Currency, TransactionResult> resultMap = new HashMap<>();
        for (Map.Entry<Currency, BigDecimal> entry : this.balanceMap.entrySet()) {
            Currency currency = entry.getKey();
            resultMap.put(currency, setBalance(currency, defaultBalance(currency), contexts));
        }
        return resultMap;
    }

    @Override
    public Map<Currency, TransactionResult> resetBalances(Cause cause) {
        final Map<Currency, TransactionResult> resultMap = new HashMap<>();
        for (Map.Entry<Currency, BigDecimal> entry : this.balanceMap.entrySet()) {
            Currency currency = entry.getKey();
            resultMap.put(currency, setBalance(currency, defaultBalance(currency), cause));
        }
        return resultMap;
    }

    @Override
    public TransactionResult resetBalance(Currency currency, Set<Context> contexts) {
        return setBalance(currency, defaultBalance(currency), contexts);
    }

    @Override
    public TransactionResult resetBalance(Currency currency, Cause cause) {
        return setBalance(currency, defaultBalance(currency), cause);
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


    private Set<Context> toContexts(Cause cause){
        return cause.all().stream().map(o -> ((Context) o)).collect(Collectors.toSet());
    }

    @Override
    public String identifier() {
        return null;
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
