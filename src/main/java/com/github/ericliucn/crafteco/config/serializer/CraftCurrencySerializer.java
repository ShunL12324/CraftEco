package com.github.ericliucn.crafteco.config.serializer;

import com.github.ericliucn.crafteco.eco.CraftCurrency;
import io.leangen.geantyref.TypeToken;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;

public class CraftCurrencySerializer implements TypeSerializer<CraftCurrency> {

    @Override
    public CraftCurrency deserialize(Type type, ConfigurationNode node) throws SerializationException {
        Component displayName = node.node("DisplayName").get(TypeToken.get(Component.class));
        Component pluralDisplayName = node.node("PluralDisplayName").get(TypeToken.get(Component.class));
        Component symbol = node.node("Symbol").get(TypeToken.get(Component.class));
        boolean isDefault = node.node("isDefault").getBoolean();
        return new CraftCurrency(displayName, pluralDisplayName, symbol, isDefault);
    }

    @Override
    public void serialize(Type type, @Nullable CraftCurrency obj, ConfigurationNode node) throws SerializationException {
        assert obj != null;
        node.node("DisplayName").set(Component.class, obj.displayName());
        node.node("PluralDisplayName").set(Component.class, obj.pluralDisplayName());
        node.node("Symbol").set(Component.class, obj.symbol());
        node.node("isDefault").set(obj.isDefault());
    }
}
