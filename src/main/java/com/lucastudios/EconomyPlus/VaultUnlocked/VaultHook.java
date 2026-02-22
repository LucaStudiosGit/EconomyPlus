package com.lucastudios.EconomyPlus.VaultUnlocked;

import com.lucastudios.EconomyPlus.Main;
import net.cfh.vault.VaultUnlockedServicesManager;

public class VaultHook {
    public static void initialize(Main plugin) {
        // This code ONLY runs if Vault is confirmed to exist.
        // The JVM won't look for VaultUnlockedEconomy until this method is hit.
        VaultUnlockedServicesManager.get().economy(new VaultUnlockedEconomy(plugin));
    }
}
