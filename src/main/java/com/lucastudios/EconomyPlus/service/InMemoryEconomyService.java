package com.lucastudios.EconomyPlus.service;

import com.lucastudios.EconomyPlus.config.PluginConfig;
import com.lucastudios.EconomyPlus.model.*;
import com.lucastudios.EconomyPlus.model.Currency;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryEconomyService {
    private final CurrencyRegistry currencyRegistry;
    private final PluginConfig config;
    private final Map<UUID, Wallet> wallets;
    private final Map<String, BaltopCache> baltopCache = new ConcurrentHashMap<>();
    private final JsonWalletStore walletStore;
    private final Path storagePath;

    public InMemoryEconomyService(CurrencyRegistry currencyRegistry, PluginConfig config, Map<UUID, Wallet> loadedWallets, JsonWalletStore walletStore, Path storagePath) {
        this.currencyRegistry = currencyRegistry;
        this.config = config;
        this.wallets = new ConcurrentHashMap<>(loadedWallets);
        this.walletStore = walletStore;
        this.storagePath = storagePath;
    }

    public Wallet getOrCreateWallet(UUID playerId, String lastKnownName) {
        Wallet existing = wallets.get(playerId);
        if (existing != null) {
            existing.updateName(lastKnownName);
            return existing;
        }

        Wallet wallet = new Wallet(playerId, lastKnownName);
        for (Map.Entry<String, Integer> entry : config.defaults().startingBalances().entrySet()) {
            Currency currency = currencyRegistry.get(entry.getKey());
            if (currency != null) {
                BigDecimal amount = currency.toMinorUnits(new BigDecimal(entry.getValue()));
                wallet.setBalance(entry.getKey(), amount);
            }
        }

        wallets.put(playerId, wallet);

        try {
            walletStore.save(storagePath, wallets);
            wallet.markClean();
        } catch (Exception e) {
            // Log error but don't fail - wallet is still in memory
        }

        return wallet;
    }

    public Wallet getWallet(UUID playerId) {
        return wallets.get(playerId);
    }

    public BigDecimal getBalance(UUID playerId, String currencyId) {
        Wallet wallet = wallets.get(playerId);
        if (wallet == null)
            return BigDecimal.ZERO;
        return wallet.getBalance(currencyId);
    }

    public TransactionResult setBalance(UUID playerId, String currencyId, BigDecimal amount) {
        Currency currency = currencyRegistry.get(currencyId);
        if (currency == null)
            return new TransactionResult.Failure(amount, BigDecimal.ZERO, TransactionResult.FailureReason.CURRENCY_NOT_FOUND, "Currency not found");

        Wallet wallet = wallets.get(playerId);
        if (wallet == null)
            return new TransactionResult.Failure(amount, BigDecimal.ZERO, TransactionResult.FailureReason.PLAYER_NOT_FOUND, "Player not found");

        if (amount.compareTo(BigDecimal.ZERO) < 0)
            return new TransactionResult.Failure(amount, wallet.getBalance(currencyId), TransactionResult.FailureReason.INVALID_AMOUNT, "Amount cannot be negative");

        wallet.setBalance(currencyId, amount);
        invalidateBaltop(currencyId);
        return new TransactionResult.Success(amount, wallet.getBalance(currencyId));
    }

    public TransactionResult addBalance(UUID playerId, String currencyId, BigDecimal amount) {
        Currency currency = currencyRegistry.get(currencyId);
        if (currency == null)
            return new TransactionResult.Failure(amount, BigDecimal.ZERO, TransactionResult.FailureReason.CURRENCY_NOT_FOUND, "Currency not found");

        Wallet wallet = wallets.get(playerId);
        if (wallet == null)
            return new TransactionResult.Failure(amount, BigDecimal.ZERO, TransactionResult.FailureReason.PLAYER_NOT_FOUND, "Player not found");

        if (amount.compareTo(BigDecimal.ZERO) < 0)
            return new TransactionResult.Failure(amount, wallet.getBalance(currencyId), TransactionResult.FailureReason.INVALID_AMOUNT, "Amount must be positive");

        wallet.addBalance(currencyId, amount);
        invalidateBaltop(currencyId);
        return new TransactionResult.Success(amount, wallet.getBalance(currencyId));
    }

    public TransactionResult takeBalance(UUID playerId, String currencyId, BigDecimal amount) {
        Currency currency = currencyRegistry.get(currencyId);
        if (currency == null)
            return new TransactionResult.Failure(amount, BigDecimal.ZERO, TransactionResult.FailureReason.CURRENCY_NOT_FOUND, "Currency not found");

        Wallet wallet = wallets.get(playerId);
        if (wallet == null)
            return new TransactionResult.Failure(amount, BigDecimal.ZERO, TransactionResult.FailureReason.PLAYER_NOT_FOUND, "Player not found");

        if (amount.compareTo(BigDecimal.ZERO) < 0)
            return new TransactionResult.Failure(amount, wallet.getBalance(currencyId), TransactionResult.FailureReason.INVALID_AMOUNT, "Amount must be positive");

        if (!wallet.takeBalance(currencyId, amount))
            return new TransactionResult.Failure(amount, wallet.getBalance(currencyId), TransactionResult.FailureReason.INSUFFICIENT_FUNDS, "Insufficient funds");

        invalidateBaltop(currencyId);
        return new TransactionResult.Success(amount, wallet.getBalance(currencyId));
    }

    public PayResult pay(UUID fromPlayerId, UUID toPlayerId, String currencyId, BigDecimal gross) {
        if (fromPlayerId.equals(toPlayerId))
            return PayResult.failure(TransactionResult.FailureReason.CANNOT_PAY_SELF);

        Currency currency = currencyRegistry.get(currencyId);
        if (currency == null)
            return PayResult.failure(TransactionResult.FailureReason.CURRENCY_NOT_FOUND);

        Wallet fromWallet = wallets.get(fromPlayerId);
        Wallet toWallet = wallets.get(toPlayerId);
        if (fromWallet == null || toWallet == null)
            return PayResult.failure(TransactionResult.FailureReason.PLAYER_NOT_FOUND);

        if (gross.compareTo(BigDecimal.ZERO) < 0)
            return PayResult.failure(TransactionResult.FailureReason.INVALID_AMOUNT);

        BigDecimal tax = calculateTax(gross);
        BigDecimal net = gross.subtract(tax);

        if (net.compareTo(BigDecimal.ZERO) < 0)
            return PayResult.failure(TransactionResult.FailureReason.FLAT_TAX_TOO_HIGH);

        if (!fromWallet.hasBalance(currencyId, gross))
            return PayResult.failure(TransactionResult.FailureReason.INSUFFICIENT_FUNDS);

        fromWallet.takeBalance(currencyId, gross);
        toWallet.addBalance(currencyId, net);

        invalidateBaltop(currencyId);
        return PayResult.success(gross, tax, net, "sink");
    }

    private BigDecimal calculateTax(BigDecimal gross) {
        PluginConfig.Tax.Pay taxConfig = config.tax().pay();
        double percentTax = gross.longValue() * taxConfig.percent() / 100.0;
        double totalTax = percentTax + taxConfig.flat();

        String rounding = taxConfig.rounding();
        if ("up".equals(rounding))
            return BigDecimal.valueOf(Math.ceil(totalTax));
        if ("nearest".equals(rounding))
            return BigDecimal.valueOf(Math.round(totalTax));
        return BigDecimal.valueOf(Math.floor(totalTax));
    }

    public List<BalanceEntry> getBaltop(String currencyId, int page, int entriesPerPage) {
        BaltopCache cache = baltopCache.get(currencyId);
        long now = System.currentTimeMillis();

        if (cache == null || now - cache.timestamp > config.baltop().cacheSeconds() * 1000L) {
            List<BalanceEntry> entries = computeBaltop(currencyId);
            cache = new BaltopCache(now, entries);
            baltopCache.put(currencyId, cache);
        }

        List<BalanceEntry> all = cache.entries;
        int start = (page - 1) * entriesPerPage;
        int end = Math.min(start + entriesPerPage, all.size());

        if (start >= all.size())
            return List.of();

        return all.subList(start, end);
    }

    public int getBaltopTotalPages(String currencyId, int entriesPerPage) {
        BaltopCache cache = baltopCache.get(currencyId);
        long now = System.currentTimeMillis();

        if (cache == null || now - cache.timestamp > config.baltop().cacheSeconds() * 1000L) {
            List<BalanceEntry> entries = computeBaltop(currencyId);
            cache = new BaltopCache(now, entries);
            baltopCache.put(currencyId, cache);
        }

        int totalEntries = cache.entries.size();
        return (int) Math.ceil((double) totalEntries / entriesPerPage);
    }

    private List<BalanceEntry> computeBaltop(String currencyId) {
        List<BalanceEntry> entries = new ArrayList<>();
        for (Wallet wallet : wallets.values()) {
            BigDecimal balance = wallet.getBalance(currencyId);
            if (balance.compareTo(BigDecimal.ZERO) > 0)
                entries.add(new BalanceEntry(wallet.playerUuid(), wallet.lastKnownName(), balance));
        }
        entries.sort((a, b) -> b.balance.compareTo(a.balance));
        return entries;
    }

    private void invalidateBaltop(String currencyId) {
        baltopCache.remove(currencyId);
    }

    public Map<UUID, Wallet> wallets() {
        return wallets;
    }

    public CurrencyRegistry currencies() {
        return currencyRegistry;
    }


    public record BalanceEntry(UUID playerId, String playerName, BigDecimal balance) {}

    private record BaltopCache(long timestamp, List<BalanceEntry> entries) {}
}
