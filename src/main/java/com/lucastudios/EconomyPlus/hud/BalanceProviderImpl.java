package com.lucastudios.EconomyPlus.hud;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.List;

public class BalanceProviderImpl implements BalanceProvider {
    @Override
    public List<CurrencyBalance> getBalances(PlayerRef playerRef, Ref<EntityStore> ref, Store<EntityStore> store) {
        return List.of(
                new CurrencyBalance("Gold", "G", 222),
                new CurrencyBalance("Gems", "GG", 111)
        );
    }
}
