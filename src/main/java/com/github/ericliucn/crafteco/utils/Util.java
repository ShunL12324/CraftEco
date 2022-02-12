package com.github.ericliucn.crafteco.utils;

import com.github.ericliucn.crafteco.handler.PapiHandler;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.placeholder.PlaceholderComponent;

import java.util.UUID;

public class Util {


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
