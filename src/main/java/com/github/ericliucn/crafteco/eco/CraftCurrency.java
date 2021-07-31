package com.github.ericliucn.crafteco.eco;

import net.kyori.adventure.text.Component;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.service.economy.Currency;

import java.math.BigDecimal;

public class CraftCurrency implements Currency {

    private final Component displayName;
    private final Component pluralDisplayName;
    private final Component symbol;
    private final boolean isDefault;
    private final BigDecimal defaultValue;

    public CraftCurrency(Component displayName, Component pluralDisplayName, Component symbol, boolean isDefault, BigDecimal defaultValue){
        this.displayName = displayName;
        this.pluralDisplayName = pluralDisplayName;
        this.symbol = symbol;
        this.isDefault = isDefault;
        this.defaultValue = defaultValue;

    }

    @Override
    public Component displayName() {
        return this.displayName;
    }

    @Override
    public Component pluralDisplayName() {
        return this.pluralDisplayName;
    }

    @Override
    public Component symbol() {
        return this.symbol;
    }

    @Override
    public Component format(BigDecimal amount, int numFractionDigits) {
        return this.format(amount);
    }

    @Override
    public int defaultFractionDigits() {
        return 2;
    }

    @Override
    public boolean isDefault() {
        return this.isDefault;
    }

    public BigDecimal defaultValue(){
        return this.defaultValue;
    }

    public DataContainer toContainer(){
        DataContainer container = DataContainer.createNew();
        container.createView().getBy
    }
}
