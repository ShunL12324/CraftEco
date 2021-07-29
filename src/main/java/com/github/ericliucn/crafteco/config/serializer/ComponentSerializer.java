package com.github.ericliucn.crafteco.config.serializer;

import com.github.ericliucn.crafteco.utils.Util;
import io.leangen.geantyref.TypeToken;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;

public class ComponentSerializer implements TypeSerializer<Component> {
    @Override
    public Component deserialize(Type type, ConfigurationNode node) throws SerializationException {
        String value = node.getString();
        if (value == null) throw new SerializationException("no value at" + node.key());
        return Util.toComponent(value);
    }

    @Override
    public void serialize(Type type, @Nullable Component obj, ConfigurationNode node) throws SerializationException {
        node.set(TypeToken.get(String.class), Util.toString(obj));
    }
}
