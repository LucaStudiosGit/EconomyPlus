package com.lucastudios.EconomyPlus.service;

import com.lucastudios.EconomyPlus.model.Currency;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class CurrencyRegistry {
    private final Map<String, Currency> currencies = new LinkedHashMap<>();

    public synchronized void register(Currency currency) {
        currencies.put(currency.currencyId().toLowerCase(), currency);
    }

    public synchronized Currency get(String currencyId) {
        if (currencyId == null)
            return null;
        return currencies.get(currencyId.toLowerCase());
    }

    public boolean exists(String currencyId) {
        return get(currencyId) != null;
    }

    public synchronized Collection<Currency> all() {
        return new ArrayList<>(currencies.values());
    }

    public synchronized List<String> keys() {
        return new ArrayList<>(currencies.keySet());
    }

    public synchronized void clear() {
        currencies.clear();
    }
}
