package com.lucastudios.EconomyPlus.service;


import com.lucastudios.EconomyPlus.config.TabTextConfig;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public final class PlayerListService {
    private String opText = "[OP] ";
    private String defaultText = "";
    private String opTagColor = "#FF0000";
    private String defaultTagColor = "#FFFFFF";
    private String opNameColor = "#FFFFFF";
    private String defaultNameColor ="#FFFFFF";
    private int refreshIntervalSeconds = 5;
    public String getOpText() { return opText; }
    public String getDefaultText() { return defaultText; }
    public String getOpTagColor() { return opTagColor; }
    public String getDefaultTagColor() { return defaultTagColor; }
    public String getOpNameColor() { return opNameColor; }
    public String getDefaultNameColor() { return defaultNameColor; }
    public int getRefreshIntervalSeconds() { return refreshIntervalSeconds; }

    public void loadTabYml(Path dataFolder) {
        // defaults
        opText = "[OP]";
        opTagColor = "#FF0000";
        opNameColor = "#ffffff";
        defaultText = "";
        defaultTagColor = "#00FF00";
        defaultNameColor = "#FFFFFF";
        refreshIntervalSeconds = 5;

        Path file = dataFolder.resolve("config.yml");
        TabTextConfig.loadOrCreate(file);
        try (InputStream in = Files.newInputStream(file)) {
            Object rootObj = new Yaml().load(in);
            if (!(rootObj instanceof Map<?, ?> root)) return;

            Object opObj = root.get("op");
            Object defaultObj = root.get("default");
            refreshIntervalSeconds = readRefreshInterval(root.get("refreshIntervalSeconds"), refreshIntervalSeconds);
            if (defaultObj instanceof Map<?, ?> def) {
                defaultText = getString(def, "text", defaultText);
                defaultTagColor = getString(def, "tagColor", defaultTagColor);
                defaultNameColor = getString(def, "nameColor", defaultNameColor);
            }
            if (opObj instanceof Map<?, ?> op)
            {
                opText = getString(op, "text", opText);
                opTagColor = getString(op, "tagColor", opTagColor);
                opNameColor = getString(op, "nameColor", opNameColor);
            }
        } catch (Exception ignored) {
        }
    }

    private static String getString(Map<?, ?> map, String key, String fallback) {
        Object v = map.get(key);
        if (v instanceof String s && !s.isBlank()) return s;
        return fallback;
    }

    private int readRefreshInterval(Object node, int fallback) {
        if (node instanceof Number number) return clampInterval(number.intValue());
        if (node instanceof String text) {
            try {
                return clampInterval(Integer.parseInt(text.trim()));
            } catch (NumberFormatException ignored) {
            }
        }
        return fallback;
    }

    private int clampInterval(int seconds) {
        if (seconds < 1) return 1;
        return Math.min(seconds, 60);
    }

    public List<String> sortNames(List<String> names) {
        //names = AddFakeNames(names);
        names.sort((name1, name2) -> {
            boolean isOp1 = name1.startsWith(getOpText());
            boolean isOp2 = name2.startsWith(getOpText());

            if (isOp1 && !isOp2) {
                return -1;
            } else if (!isOp1 && isOp2) {
                return 1;
            } else {
                return name1.compareToIgnoreCase(name2);
            }
        });
        return names;
    }

    private List<String> AddFakeNames(List<String> names) {
        Collections.shuffle(testNames);
        int addedNamesCount = new Random().nextInt(35, testNames.size());
        List<String> addedNames = testNames.subList(0, addedNamesCount);
        for (String name : addedNames) {
            name = isOP(name) ? getOpText() : getDefaultText() + name;
            names.add(name);
        }
        return names;
    }

    private boolean isOP(String name) {
        String prefix = getOpText();
        return name.startsWith(prefix);
    }

    List<String> testNames = new ArrayList<>(List.of(
            "Steve",
            "Alex",
            "ShadowWolf",
            "PixelKnight",
            "VoidRunner",
            "CrystalMage",
            "IronClaw",
            "FrostByte",
            "RedstonePro",
            "NightFalcon",
            "Echo",
            "Nova",
            "BlazeKing",
            "SilverFox",
            "EnderSoul",
            "SkyWeaver",
            "GrimByte",
            "LunarX",
            "StoneGiant",
            "Phantom",
            "StormRider",
            "DarkComet",
            "GlowSpark",
            "AshWalker",
            "RuneMaster",
            "FlameWarden",
            "IceSpecter",
            "ThunderHex",
            "VoidSpark",
            "Obsidian",
            "ArcaneLord",
            "CyberFox",
            "DriftShadow",
            "SolarFlare",
            "NightRune",
            "IronSpecter",
            "MysticWave",
            "StarBreaker",
            "CinderGhost",
            "MoonCipher",
            "AdminOne",
            "WarpStriker",
            "BladeEcho",
            "QuantumAsh",
            "CoreAdmin",
            "PulseKnight",
            "FeralByte",
            "GhostSignal",
            "ServerOwner"
    ));

}
