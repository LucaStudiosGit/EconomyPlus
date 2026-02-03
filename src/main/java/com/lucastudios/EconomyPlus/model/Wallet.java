package com.lucastudios.EconomyPlus.model;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class Wallet {
    private final UUID playerUuid;
    private String lastKnownName;
    private final Map<String, Long> balances;
    private boolean dirty;

    public Wallet(UUID playerUuid, String lastKnownName) {
        this.playerUuid = playerUuid;
        this.lastKnownName = lastKnownName;
        this.balances = new ConcurrentHashMap<>();
        this.dirty = false;
    }

    public UUID playerUuid() {
        return playerUuid;
    }

    public String lastKnownName() {
        return lastKnownName;
    }

    public void updateName(String name) {
        if (name != null && !name.equals(lastKnownName)) {
            this.lastKnownName = name;
            this.dirty = true;
        }
    }

    public long getBalance(String currencyId) {
        return balances.getOrDefault(currencyId.toLowerCase(), 0L);
    }

    public void setBalance(String currencyId, Double amount) {
        if (amount < 0)
            amount = (double) 0;
        balances.put(currencyId.toLowerCase(), (long) amount.intValue());
        this.dirty = true;
    }

    public boolean hasBalance(String currencyId, long amount) {
        return getBalance(currencyId) >= amount;
    }

    public void addBalance(String currencyId, long amount) {
        long current = getBalance(currencyId);
        setBalance(currencyId, (double) (current + amount));
    }

    public boolean takeBalance(String currencyId, long amount) {
        long current = getBalance(currencyId);
        if (current < amount)
            return false;
        setBalance(currencyId, (double) (current - amount));
        return true;
    }

    public Map<String, Long> balances() {
        return new ConcurrentHashMap<>(balances);
    }

    public boolean isDirty() {
        return dirty;
    }

    public void markClean() {
        this.dirty = false;
    }

    public void markDirty() {
        this.dirty = true;
    }
}
