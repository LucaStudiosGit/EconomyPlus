package com.lucastudios.EconomyPlus.service;

import com.hypixel.hytale.server.core.NameMatching;
import com.hypixel.hytale.server.core.universe.Universe;
import com.lucastudios.EconomyPlus.Main;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.UUID;

public class Utility {
    public static UUID GetUuidByPlayerName(Main main, String playerName) throws IOException {
        if (main.walletStore.getUUIDFromUsername(playerName) != null) {
            return main.walletStore.getUUIDFromUsername(playerName);
        }
        if (Universe.get().getPlayerByUsername(playerName, NameMatching.EXACT) == null) {
            return null;
        }
        return Universe.get().getPlayerByUsername(playerName, NameMatching.EXACT).getUuid();
    }
}
