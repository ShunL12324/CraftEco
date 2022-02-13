package com.github.ericliucn.crafteco.eco.account;

import org.spongepowered.api.service.economy.account.VirtualAccount;

import java.util.UUID;

public class CraftVirtualAccount extends CraftAccount implements VirtualAccount {

    public CraftVirtualAccount(String identifier, UUID uniqueID) {
        super(identifier, uniqueID);
        this.isVirtual = true;
    }

}
