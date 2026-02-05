package com.lucastudios.EconomyPlus.config;

import java.util.Map;

public record PluginConfig(
    Defaults defaults,
    Tax tax,
    Hud hud,
    BaltopConfig baltop,
    Storage storage
) {
    public record Defaults(
        String primaryCurrency,
        Map<String, Integer> startingBalances
    ) {}

    public record Tax(
        Pay pay
    ) {
        public record Pay(
            double percent,
            int flat,
            String rounding
        ) {}
    }

    public record Hud(
        String currency
    ) {}

    public record BaltopConfig(
        int cacheSeconds,
        int entriesPerPage
    ) {}

    public record Storage(
        String file,
        int autosaveSeconds
    ) {}
}
