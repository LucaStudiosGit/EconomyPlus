package com.lucastudios.EconomyPlus.service;

import com.hypixel.hytale.server.core.NameMatching;
import com.hypixel.hytale.server.core.universe.Universe;

import javax.annotation.Nullable;
import java.util.UUID;

public class Utility {
    public static UUID GetUuidByPlayerName(String playerName) {
        if (Universe.get().getPlayerByUsername(playerName, NameMatching.EXACT) == null) {
            return null;
        }
        return Universe.get().getPlayerByUsername(playerName, NameMatching.EXACT).getUuid();
    }
}
