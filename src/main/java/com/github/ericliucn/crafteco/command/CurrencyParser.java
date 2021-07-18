package com.github.ericliucn.crafteco.command;

import com.github.ericliucn.crafteco.Main;
import com.github.ericliucn.crafteco.eco.CraftCurrency;
import com.github.ericliucn.crafteco.utils.ComponentUtil;
import org.spongepowered.api.command.exception.ArgumentParseException;
import org.spongepowered.api.command.parameter.ArgumentReader;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.managed.ValueParser;

import java.util.Optional;

class CraftCurrencyParser implements ValueParser<CraftCurrency> {


    @Override
    public Optional<? extends CraftCurrency> parseValue(Parameter.Key<? super CraftCurrency> parameterKey, ArgumentReader.Mutable reader, CommandContext.Builder context) throws ArgumentParseException {
        reader.skipWhitespace();
        String currencyName = reader.parseString();
        for (CraftCurrency currency : Main.instance.getCraftEcoService().currencies) {
            if (ComponentUtil.toPlain(currency.displayName()).equalsIgnoreCase(currencyName)){
                return Optional.of(currency);
            }
        }
        return Optional.empty();
    }
}
