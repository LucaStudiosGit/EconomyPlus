package com.lucastudios.EconomyPlus.service;

import com.lucastudios.EconomyPlus.model.Currency;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class CurrencyRegistry {
    private final Map<String, Currency> currencies = new ConcurrentHashMap<>();

    public void register(Currency currency) {
        currencies.put(currency.currencyId().toLowerCase(), currency);
    }

    public Currency get(String currencyId) {
        if (currencyId == null)
            return null;
        return currencies.get(currencyId.toLowerCase());
    }

    public boolean exists(String currencyId) {
        return get(currencyId) != null;
    }

    public Collection<Currency> all() {
        return currencies.values();
    }

    public Collection<String> keys() {
        return currencies.keySet();
    }

    public void clear() {
        currencies.clear();
    }
}
