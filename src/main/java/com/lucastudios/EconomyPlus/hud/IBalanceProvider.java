package com.lucastudios.EconomyPlus.hud;

import com.hypixel.hytale.server.core.universe.PlayerRef;

import java.util.List;

public interface IBalanceProvider {
    List<CurrencyBalance> getBalances(PlayerRef playerRef);
}

