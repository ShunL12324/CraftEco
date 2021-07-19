package com.github.ericliucn.crafteco.utils;

import com.github.ericliucn.crafteco.eco.CraftUniqueAccount;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.service.economy.account.Account;

public class AccountUtil {

    public static String serialize(Account account){
        if (account instanceof CraftUniqueAccount){
            DataContainer dataContainer = DataContainer.createNew()
        }
    }

}
