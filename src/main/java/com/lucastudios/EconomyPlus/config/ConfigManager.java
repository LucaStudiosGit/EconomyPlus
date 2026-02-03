package com.lucastudios.EconomyPlus.config;

import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

public final class ConfigManager {
    private final Yaml yaml = new Yaml();

    public PluginConfig loadConfig(Path file) throws Exception {
        Map<String, Object> data = loadYaml(file);

        Map<String, Object> defaultsNode = getMap(data, "defaults");
        String primaryCurrency = getString(defaultsNode, "primary-currency", "coins");
        Map<String, Integer> startingBalances = getIntMap(defaultsNode, "starting-balances");
        PluginConfig.Defaults defaults = new PluginConfig.Defaults(primaryCurrency, startingBalances);

        Map<String, Object> taxNode = getMap(data, "tax");
        Map<String, Object> taxPayNode = getMap(taxNode, "pay");
        double taxPercent = getDouble(taxPayNode, "percent", 0.0);
        int taxFlat = getInt(taxPayNode, "flat", 0);
        String taxRounding = getString(taxPayNode, "rounding", "down");
        PluginConfig.Tax.Pay taxPay = new PluginConfig.Tax.Pay(taxPercent, taxFlat, taxRounding);
        PluginConfig.Tax tax = new PluginConfig.Tax(taxPay);

        Map<String, Object> hudNode = getMap(data, "hud");
        boolean hudEnabled = getBoolean(hudNode, "enabled", true);
        String hudCurrency = getString(hudNode, "currency", "coins");
        int hudRefresh = getInt(hudNode, "refresh-seconds", 2);
        String hudAnchor = getString(hudNode, "anchor", "TOP_RIGHT");
        int hudOffsetX = getInt(hudNode, "offset-x", -20);
        int hudOffsetY = getInt(hudNode, "offset-y", 20);
        boolean hudShowName = getBoolean(hudNode, "show-currency-name", false);
        PluginConfig.Hud hud = new PluginConfig.Hud(hudEnabled, hudCurrency, hudRefresh, hudAnchor, hudOffsetX, hudOffsetY, hudShowName);

        Map<String, Object> baltopNode = getMap(data, "baltop");
        int baltopCache = getInt(baltopNode, "cache-seconds", 30);
        int baltopEntries = getInt(baltopNode, "entries-per-page", 10);
        PluginConfig.BaltopConfig baltop = new PluginConfig.BaltopConfig(baltopCache, baltopEntries);

        Map<String, Object> storageNode = getMap(data, "storage");
        String storageFile = getString(storageNode, "file", "balances.json");
        int autosaveSeconds = getInt(storageNode, "autosave-seconds", 30);
        PluginConfig.Storage storage = new PluginConfig.Storage(storageFile, autosaveSeconds);

        return new PluginConfig(defaults, tax, hud, baltop, storage);
    }

    public CurrencyConfig loadCurrencies(Path file) throws Exception {
        Map<String, Object> data = loadYaml(file);
        Map<String, Object> currencies = getMap(data, "currencies");
        return new CurrencyConfig(currencies);
    }

    public MessagesConfig loadMessages(Path file) throws Exception {
        Map<String, Object> data = loadYaml(file);
        Map<String, String> messages = new HashMap<>();
        for (Map.Entry<String, Object> entry : data.entrySet())
            messages.put(entry.getKey(), String.valueOf(entry.getValue()));
        return new MessagesConfig(messages);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> loadYaml(Path file) throws Exception {
        try (InputStream in = Files.newInputStream(file)) {
            Object loaded = yaml.load(in);
            if (loaded instanceof Map<?, ?>)
                return (Map<String, Object>) loaded;
            return new HashMap<>();
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getMap(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value instanceof Map<?, ?>)
            return (Map<String, Object>) value;
        return new HashMap<>();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Integer> getIntMap(Map<String, Object> data, String key) {
        Map<String, Integer> result = new HashMap<>();
        Object value = data.get(key);
        if (value instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String k = String.valueOf(entry.getKey());
                if (entry.getValue() instanceof Number n)
                    result.put(k, n.intValue());
            }
        }
        return result;
    }

    private String getString(Map<String, Object> data, String key, String defaultValue) {
        Object value = data.get(key);
        return value != null ? String.valueOf(value) : defaultValue;
    }

    private int getInt(Map<String, Object> data, String key, int defaultValue) {
        Object value = data.get(key);
        if (value instanceof Number n)
            return n.intValue();
        return defaultValue;
    }

    private double getDouble(Map<String, Object> data, String key, double defaultValue) {
        Object value = data.get(key);
        if (value instanceof Number n)
            return n.doubleValue();
        return defaultValue;
    }

    private boolean getBoolean(Map<String, Object> data, String key, boolean defaultValue) {
        Object value = data.get(key);
        if (value instanceof Boolean b)
            return b;
        return defaultValue;
    }

    public static void writeDefaultResource(Path target, InputStream resource) throws IOException {
        if (resource == null)
            throw new IOException("Resource not found");
        Files.createDirectories(target.getParent());
        Files.copy(resource, target, StandardCopyOption.REPLACE_EXISTING);
    }
}
