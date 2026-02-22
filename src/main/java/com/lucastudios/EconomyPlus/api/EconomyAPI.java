package com.lucastudios.EconomyPlus.api;

import com.lucastudios.EconomyPlus.model.*;
import com.lucastudios.EconomyPlus.service.InMemoryEconomyService;
import net.milkbowl.vault2.economy.EconomyResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public final class EconomyAPI {
    private static InMemoryEconomyService service;

    public static void setService(InMemoryEconomyService economyService) {
        service = economyService;
    }

    public static BigDecimal getBalance(UUID playerId, String currencyId) {
        if (service == null)
            return BigDecimal.ZERO;
        return service.getBalance(playerId, currencyId);
    }

    public static TransactionResult setBalance(UUID playerId, String currencyId, BigDecimal amount) {
        if (service == null)
            return new TransactionResult.Failure(amount, BigDecimal.ZERO, TransactionResult.FailureReason.PLAYER_NOT_FOUND, "Service not initialized");
        return service.setBalance(playerId, currencyId, amount);
    }

    public static TransactionResult deposit(UUID playerId, String currencyId, BigDecimal amount) {
        if (service == null)
            return new TransactionResult.Failure(amount, BigDecimal.ZERO, TransactionResult.FailureReason.PLAYER_NOT_FOUND, "Service not initialized");
        return service.addBalance(playerId, currencyId, amount);
    }

    public static TransactionResult withdraw(UUID playerId, String currencyId, BigDecimal amount) {
        if (service == null)
            return new TransactionResult.Failure(amount, BigDecimal.ZERO, TransactionResult.FailureReason.PLAYER_NOT_FOUND, "Service not initialized");
        return service.takeBalance(playerId, currencyId, amount);
    }

    public static PayResult pay(UUID fromPlayerId, UUID toPlayerId, String currencyId, BigDecimal amount) {
        if (service == null)
            return PayResult.failure(TransactionResult.FailureReason.PLAYER_NOT_FOUND);
        return service.pay(fromPlayerId, toPlayerId, currencyId, amount);
    }

    public static boolean currencyExists(String currencyId) {
        if (service == null)
            return false;
        return service.currencies().exists(currencyId);
    }

        public static List<String> getCurrencies() {
        if (service == null)
            return List.of();
        return List.copyOf(service.currencies().keys());
    }
}
