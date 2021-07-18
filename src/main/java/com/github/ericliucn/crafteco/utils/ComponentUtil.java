package com.github.ericliucn.crafteco.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class ComponentUtil {

    public static TextComponent toComponent(String str){
        return LegacyComponentSerializer.legacyAmpersand().deserialize(str);
    }

    public static String toString(Component component){
        return LegacyComponentSerializer.legacyAmpersand().serialize(component);
    }

    public static String toPlain(Component component){
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

}
