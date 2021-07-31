package com.github.ericliucn.crafteco.eco.account;

import org.spongepowered.api.service.economy.account.VirtualAccount;

import java.util.UUID;

public class CraftVirtualAccount extends CraftAccount implements VirtualAccount {

    private final String name;

    public CraftVirtualAccount(String name, UUID uniqueID) {
        super(uniqueID);
        this.name = name;
        this.isVirtual = true;
    }

    @Override
    public String identifier() {
        return this.name;
    }
}
