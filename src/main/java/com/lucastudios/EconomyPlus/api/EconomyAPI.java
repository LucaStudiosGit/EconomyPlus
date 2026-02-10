package com.lucastudios.EconomyPlus.api;

import com.lucastudios.EconomyPlus.model.*;
import com.lucastudios.EconomyPlus.service.InMemoryEconomyService;

import java.util.List;
import java.util.UUID;

public final class EconomyAPI {
    private static InMemoryEconomyService service;

    public static void setService(InMemoryEconomyService economyService) {
        service = economyService;
    }

    public static long getBalance(UUID playerId, String currencyId) {
        if (service == null)
            return 0;
        return service.getBalance(playerId, currencyId);
    }

    public static TransactionResult setBalance(UUID playerId, String currencyId, Double amount) {
        if (service == null)
            return new TransactionResult.Failure(TransactionResult.FailureReason.PLAYER_NOT_FOUND, "Service not initialized");
        return service.setBalance(playerId, currencyId, amount);
    }

    public static TransactionResult addBalance(UUID playerId, String currencyId, long amount) {
        if (service == null)
            return new TransactionResult.Failure(TransactionResult.FailureReason.PLAYER_NOT_FOUND, "Service not initialized");
        return service.addBalance(playerId, currencyId, amount);
    }

    public static TransactionResult takeBalance(UUID playerId, String currencyId, long amount) {
        if (service == null)
            return new TransactionResult.Failure(TransactionResult.FailureReason.PLAYER_NOT_FOUND, "Service not initialized");
        return service.takeBalance(playerId, currencyId, amount);
    }

    public static PayResult pay(UUID fromPlayerId, UUID toPlayerId, String currencyId, long amount) {
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
