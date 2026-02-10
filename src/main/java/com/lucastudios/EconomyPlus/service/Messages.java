package com.lucastudios.EconomyPlus.service;

import com.lucastudios.EconomyPlus.config.MessagesConfig;

import java.util.HashMap;
import java.util.Map;

public final class Messages {
    private Map<String, String> messages;

    public Messages(MessagesConfig config) {
        this.messages = new HashMap<>(config.messages());
    }

    public void reload(MessagesConfig config) {
        this.messages = new HashMap<>(config.messages());
    }

    public String get(String key) {
        return messages.getOrDefault(key, "Message missing: " + key);
    }

    public String format(String key, Map<String, String> placeholders) {
        String message = get(key);
        for (Map.Entry<String, String> entry : placeholders.entrySet())
            message = message.replace("{" + entry.getKey() + "}", entry.getValue());
        return translateColorCodes(message);
    }

    public String format(String key) {
        return format(key, Map.of());
    }

    private String translateColorCodes(String text) {
        if (text == null)
            return "";
        return text.replace("&", "§");
    }
}
