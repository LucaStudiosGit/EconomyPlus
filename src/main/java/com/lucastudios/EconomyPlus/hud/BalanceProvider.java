package com.lucastudios.EconomyPlus.hud;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.List;

/**
 * Plug this into your economy system.
 * Return the player's balances (multiple currencies supported).
 */
public interface BalanceProvider {
    List<CurrencyBalance> getBalances(PlayerRef playerRef);
}

