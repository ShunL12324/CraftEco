package com.github.ericliucn.crafteco.eco.account;

import org.spongepowered.api.service.economy.account.VirtualAccount;

import java.util.UUID;

public class CraftVirtualAccount extends CraftAccount implements VirtualAccount {

    public CraftVirtualAccount(String name, UUID uniqueID) {
        super(uniqueID, name);
        this.isVirtual = true;
    }

}
