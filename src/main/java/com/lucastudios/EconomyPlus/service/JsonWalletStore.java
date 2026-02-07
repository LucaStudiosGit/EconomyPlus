package com.lucastudios.EconomyPlus.service;

import com.google.gson.*;
import com.lucastudios.EconomyPlus.model.Wallet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class JsonWalletStore {
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private Path file;

    public Map<UUID, Wallet> load(Path file) throws IOException {
        Map<UUID, Wallet> wallets = new HashMap<>();

        if (!Files.exists(file))
            return wallets;

        String json = Files.readString(file);
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();

        if (!root.has("players"))
            return wallets;

        JsonObject playersNode = root.getAsJsonObject("players");
        for (Map.Entry<String, JsonElement> entry : playersNode.entrySet()) {
            try {
                UUID playerId = UUID.fromString(entry.getKey());
                Wallet wallet = getWallet(entry, playerId);
                wallets.put(playerId, wallet);
            } catch (Exception ignored) {}
        }

        return wallets;
    }

    private static Wallet getWallet(Map.Entry<String, JsonElement> entry, UUID playerId) {
        JsonObject balancesNode = entry.getValue().getAsJsonObject();

        Wallet wallet = new Wallet(playerId, "Unknown");
        for (Map.Entry<String, JsonElement> balEntry : balancesNode.entrySet()) {
            String currencyId = balEntry.getKey();
            if ("lastKnownName".equals(currencyId)) {
                wallet.updateName(balEntry.getValue().getAsString());
                continue;
            }
            long balance = balEntry.getValue().getAsLong();
            wallet.setBalance(currencyId, (double) balance);
        }
        wallet.markClean();
        return wallet;
    }

    public void save(Path file, Map<UUID, Wallet> wallets) throws IOException {
        this.file = file;
        Map<UUID, Wallet> existingWallets = new HashMap<>();
        if (Files.exists(file)) {
            existingWallets = load(file);
        }

        JsonObject root = new JsonObject();
        root.addProperty("schema", 1);

        JsonObject playersNode = new JsonObject();

        for (Wallet existingWallet : existingWallets.values()) {
            JsonObject balancesNode = new JsonObject();
            balancesNode.addProperty("lastKnownName", existingWallet.lastKnownName());
            for (Map.Entry<String, Long> entry : existingWallet.balances().entrySet())
                balancesNode.addProperty(entry.getKey(), entry.getValue());
            playersNode.add(existingWallet.playerUuid().toString(), balancesNode);
        }

        for (Wallet wallet : wallets.values()) {
            if (!wallet.isDirty())
                continue;

            JsonObject balancesNode = new JsonObject();
            balancesNode.addProperty("lastKnownName", wallet.lastKnownName());
            for (Map.Entry<String, Long> entry : wallet.balances().entrySet())
                balancesNode.addProperty(entry.getKey(), entry.getValue());

            playersNode.add(wallet.playerUuid().toString(), balancesNode);
            wallet.markClean();
        }

        root.add("players", playersNode);

        Path temp = file.getParent().resolve(file.getFileName() + ".tmp");
        Files.writeString(temp, gson.toJson(root));
        Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }

    public UUID getUUIDFromUsername(String username) throws IOException {
        Map<UUID, Wallet> existingWallets = new HashMap<>();
        if (Files.exists(file)) {
            existingWallets = load(file);
        }
        for (Map.Entry<UUID, Wallet> entry : existingWallets.entrySet()) {
            if (entry.getValue().lastKnownName().equalsIgnoreCase(username)) {
                return entry.getKey();
            }
        }
        return null;
    }
}
