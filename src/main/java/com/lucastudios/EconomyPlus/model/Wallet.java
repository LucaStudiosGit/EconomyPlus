package com.lucastudios.EconomyPlus.model;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class Wallet {
    private final UUID playerUuid;
    private String lastKnownName;
    private final Map<String, BigDecimal> balances;
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

    public BigDecimal getBalance(String currencyId) {
        return balances.getOrDefault(currencyId.toLowerCase(), BigDecimal.ZERO);
    }


    public void setBalance(String currencyId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0)
            amount = BigDecimal.ZERO;
        balances.put(currencyId.toLowerCase(), amount);
        this.dirty = true;
    }

    public boolean hasBalance(String currencyId, BigDecimal amount) {
        return amount.compareTo(getBalance(currencyId)) >= 0;
    }

    public void addBalance(String currencyId, BigDecimal amount) {
        BigDecimal current = getBalance(currencyId);
        setBalance(currencyId, amount.add(current));
    }

    public boolean takeBalance(String currencyId, BigDecimal amount) {
        BigDecimal current = getBalance(currencyId);
        if (current.compareTo(amount) < 0)
            return false;
        setBalance(currencyId, current.subtract(amount));
        return true;
    }

    public Map<String, BigDecimal> balances() {
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
