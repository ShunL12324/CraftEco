package com.github.ericliucn.crafteco.eco.account;

import com.github.ericliucn.crafteco.config.ConfigLoader;
import com.github.ericliucn.crafteco.config.CraftEcoConfig;
import com.github.ericliucn.crafteco.eco.CraftCurrency;
import com.github.ericliucn.crafteco.eco.CraftEcoService;
import com.github.ericliucn.crafteco.eco.CraftResult;
import com.github.ericliucn.crafteco.handler.PapiHandler;
import com.github.ericliucn.crafteco.utils.Contexts;
import com.github.ericliucn.crafteco.utils.Util;
import com.google.gson.Gson;
import io.leangen.geantyref.TypeToken;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.EventContext;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.account.Account;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.service.economy.transaction.TransactionResult;
import org.spongepowered.api.service.economy.transaction.TransactionTypes;
import org.spongepowered.api.service.economy.transaction.TransferResult;
import org.spongepowered.api.service.pagination.PaginationList;

import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

public class CraftAccount implements Account, UniqueAccount {

    private Map<Currency, BigDecimal> balanceMap;
    private final UUID uniqueID;
    protected boolean isVirtual;
    private String identifier;

    public CraftAccount(final String identifier, final UUID uniqueID){
        this.balanceMap = new ConcurrentHashMap<>();
        this.uniqueID = uniqueID;
        this.isVirtual = false;
        // the default identifier is the uuid, will be change if any player's uuid match the account's uuid
        this.identifier = identifier;
    }

    @Override
    public Component displayName() {
        return Util.toComponent(this.identifier());
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
    public TransactionResult setBalance(Currency currency, BigDecimal amount) {
        this.balanceMap.put(currency, amount);
        return CraftResult.builder()
                .currency(currency)
                .account(this)
                .amount(amount)
                .result(ResultType.SUCCESS)
                .build();
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
        Map<Currency, TransactionResult> results = new HashMap<>();
        CraftEcoService.instance.currencies().forEach(cur -> {
            TransactionResult result = this.setBalance(cur, defaultBalance(cur));
            results.put(cur, result);
        });
        return results;
    }

    @Override
    public Map<Currency, TransactionResult> resetBalances(Set<Context> contexts) {
        Map<Currency, TransactionResult> results = resetBalances();
        resetBalances().replaceAll((currency, transactionResult) -> {
            return ((CraftResult) transactionResult).toBuilder().contexts(contexts).build();
        });
        return results;
    }

    @Override
    public Map<Currency, TransactionResult> resetBalances(Cause cause) {
        Map<Currency, TransactionResult> results = resetBalances();
        resetBalances().replaceAll((currency, transactionResult) -> {
            return ((CraftResult) transactionResult).toBuilder().cause(cause).build();
        });
        return results;
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
        CraftResult.Builder result = CraftResult.builder()
                .currency(currency)
                .account(this)
                .type(TransactionTypes.DEPOSIT.get())
                .amount(amount);
        try{
            this.balanceMap.put(currency, balance(currency).add(amount));
            return result.result(ResultType.SUCCESS).build();
        }catch (Exception e){
            e.printStackTrace();
            return result.result(ResultType.FAILED).build();
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
        CraftResult.Builder result = CraftResult.builder()
                .currency(currency)
                .account(this)
                .type(TransactionTypes.WITHDRAW.get())
                .amount(amount);
        try {
            if (balance(currency).compareTo(amount) < 0) {
                return result.result(ResultType.ACCOUNT_NO_FUNDS).build();
            }
            this.balanceMap.put(currency, balance(currency).subtract(amount));
            return result.result(ResultType.SUCCESS).build();
        }catch (Exception e){
            e.printStackTrace();
            return result.result(ResultType.FAILED).build();
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
        CraftResult.Builder result = CraftResult.builder()
                .account(this)
                .type(TransactionTypes.TRANSFER.get())
                .amount(amount)
                .currency(currency);

        if (balance(currency).compareTo(amount) < 0){
            return result.result(ResultType.ACCOUNT_NO_FUNDS).build().toTransferResult(to);
        }else {
            try {
                TransactionResult withdrawResult = this.withdraw(currency, amount);
                TransactionResult depositResult = to.deposit(currency, amount);
                if (withdrawResult.result().equals(ResultType.SUCCESS) && depositResult.result().equals(ResultType.SUCCESS)){
                    return result.result(ResultType.FAILED).build().toTransferResult(to);
                }else {
                    throw new Exception("error occurred during transfer");
                }
            }catch (Exception e){
                e.printStackTrace();
                return result.result(ResultType.FAILED).build().toTransferResult(to);
            }
        }
    }

    @Override
    public TransferResult transfer(Account to, Currency currency, BigDecimal amount, Set<Context> contexts) {
        return ((CraftResult.CraftTransferResult) transfer(to, currency, amount)).withContext(contexts);
    }

    @Override
    public TransferResult transfer(Account to, Currency currency, BigDecimal amount, Cause cause) {
        return ((CraftResult.CraftTransferResult) transfer(to, currency, amount)).withCause(cause);
    }

    @Override
    public String identifier() {
        return this.identifier;
    }

    @Override
    public UUID uniqueId() {
        return this.uniqueID;
    }

    public void setIdentifier(String identifier){
        this.identifier = identifier;
    }

    public boolean isVirtual(){
        return this.isVirtual;
    }

    private static final Gson GSON = new Gson();
    private static final Type TYPE = new TypeToken<Map<String, Map<String, String>>>(){}.getType();

    public byte[] serialize() throws IOException {
        Map<String , Map<String, String>> data = new HashMap<>();
        Map<String, String> properties = new HashMap<>();
        Map<String,String> balanceData = new HashMap<>();
        properties.put("uuid", this.uniqueID.toString());
        properties.put("identifier", this.identifier());
        properties.put("isVirtual", this.isVirtual ? "true":"false");
        for (Map.Entry<Currency, BigDecimal> entry : this.balanceMap.entrySet()) {
            CraftCurrency currency = ((CraftCurrency) entry.getKey());
            balanceData.put(currency.toPlain(), entry.getValue().toString());
        }
        data.put("properties", properties);
        data.put("balanceData", balanceData);
        return GSON.toJson(data).getBytes(StandardCharsets.UTF_8);
    }

    @Nullable
    public static CraftAccount deserialize(byte[] bytes) {
        try {
            String dataStr = new String(bytes, StandardCharsets.UTF_8);
            Map<String , Map<String, String>> data = GSON.fromJson(dataStr, TYPE);
            Map<String, String> properties = data.get("properties");
            Map<String,String> balanceData = data.get("balanceData");
            UUID uuid = UUID.fromString(properties.get("uuid"));
            String identifier = properties.get("identifier");
            boolean isVirtual = properties.get("isVirtual").equals("true");
            CraftAccount account = isVirtual ? new CraftVirtualAccount(identifier, uuid) : new CraftAccount(identifier, uuid);
            Map<Currency, BigDecimal> balMap = new HashMap<>();
            for (Map.Entry<String, String> entry : balanceData.entrySet()) {
                for (CraftCurrency currency : ConfigLoader.instance.getConfig().currencies) {
                    if (entry.getKey().equals(currency.toPlain())){
                        balMap.put(currency, new BigDecimal(entry.getValue()));
                    }
                }
            }
            account.balanceMap = balMap;
            return account;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
