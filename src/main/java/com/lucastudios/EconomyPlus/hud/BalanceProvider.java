package com.lucastudios.EconomyPlus.hud;

import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.lucastudios.EconomyPlus.model.Currency;
import com.lucastudios.EconomyPlus.model.Wallet;
import com.lucastudios.EconomyPlus.service.CurrencyRegistry;
import com.lucastudios.EconomyPlus.service.InMemoryEconomyService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class BalanceProvider implements IBalanceProvider {

    private final InMemoryEconomyService economy;
    private final CurrencyRegistry currencyRegistry;

    public BalanceProvider(InMemoryEconomyService economy, CurrencyRegistry currencyRegistry) {
        this.economy = economy;
        this.currencyRegistry = currencyRegistry;
    }

    @Override
    public List<CurrencyBalance> getBalances(PlayerRef playerRef) {
        if (playerRef == null)
            return List.of();

        Wallet wallet = economy.getWallet(playerRef.getUuid());
        if (wallet == null)
            return List.of();

        List<CurrencyBalance> balances = new ArrayList<>();

        for (String currencyId : currencyRegistry.keys()) {
            Currency currency = currencyRegistry.get(currencyId);
            if (currency == null)
                continue;

            BigDecimal balance = wallet.getBalance(currencyId);
            balances.add(new CurrencyBalance(currency.name(), currency.symbol(), balance));
        }

        return balances;
    }
}
