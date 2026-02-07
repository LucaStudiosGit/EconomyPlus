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
        Map<String, Integer> startingBalances = getIntMap(defaultsNode);
        PluginConfig.Defaults defaults = new PluginConfig.Defaults(primaryCurrency, startingBalances);

        Map<String, Object> taxNode = getMap(data, "tax");
        Map<String, Object> taxPayNode = getMap(taxNode, "pay");
        double taxPercent = getDouble(taxPayNode);
        int taxFlat = getInt(taxPayNode, "flat", 0);
        String taxRounding = getString(taxPayNode, "rounding", "down");
        PluginConfig.Tax.Pay taxPay = new PluginConfig.Tax.Pay(taxPercent, taxFlat, taxRounding);
        PluginConfig.Tax tax = new PluginConfig.Tax(taxPay);

        Map<String, Object> hudNode = getMap(data, "hud");
        String hudCurrency = getString(hudNode, "currency", "all");
        PluginConfig.Hud hud = new PluginConfig.Hud(hudCurrency);

        int baltopCache = 30;
        int baltopEntries = 42;
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

    private Map<String, Object> loadYaml(Path file) throws Exception {
        try (InputStream in = Files.newInputStream(file)) {
            Object loaded = yaml.load(in);
            if (loaded instanceof Map<?, ?>)
                return (Map<String, Object>) loaded;
            return new HashMap<>();
        }
    }

    private Map<String, Object> getMap(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value instanceof Map<?, ?>)
            return (Map<String, Object>) value;
        return new HashMap<>();
    }

    private Map<String, Integer> getIntMap(Map<String, Object> data) {
        Map<String, Integer> result = new HashMap<>();
        Object value = data.get("starting-balances");
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

    private double getDouble(Map<String, Object> data) {
        Object value = data.get("percent");
        if (value instanceof Number n)
            return n.doubleValue();
        return 0.0;
    }

    public static void writeDefaultResource(Path target, InputStream resource) throws IOException {
        if (resource == null)
            throw new IOException("Resource not found");
        Files.createDirectories(target.getParent());
        Files.copy(resource, target, StandardCopyOption.REPLACE_EXISTING);
    }
}
