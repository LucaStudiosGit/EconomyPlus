package com.lucastudios.EconomyPlus.config;

import com.hypixel.hytale.logger.HytaleLogger;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public record TabTextConfig(String adminPrefix) {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final Yaml YAML = new Yaml();

    public TabTextConfig(String adminPrefix) {
        this.adminPrefix = adminPrefix;
    }


    public static TabTextConfig loadOrCreate(Path path) {
        try {
            if (Files.notExists(path)) {
                writeDefault(path);
            }

            try (InputStream in = Files.newInputStream(path)) {
                Map<String, Object> data = YAML.load(in);

                String adminPrefix = null;

                if (data != null) {
                    adminPrefix = (String) data.get("adminPrefix");
                }

                return new TabTextConfig(
                        adminPrefix != null ? adminPrefix : defaultAdminPrefix()
                );
            }

        } catch (Exception e) {
            LOGGER.atWarning().withCause(e).log("Failed to load config.yml, using defaults");
            return new TabTextConfig(defaultAdminPrefix());
        }
    }

    private static void writeDefault(Path path) throws Exception {
        Files.createDirectories(path.getParent());

        String defaultYaml =
                """
                # =========================
                # TAB, Player List Settings
                # =========================
                #
                # Colors are hex strings, example, "#FFFFFF"
                #
        
                default:
                  # Prefix shown for regular players
                  text: "[MEMBER] "
                  # Prefix color
                  tagColor: "#0000FF"
                  # Player name color
                  nameColor: "#FFFFFF"
        
                op:
                  # Prefix shown for operators
                  text: "[OP] "
                  # Prefix color
                  tagColor: "#FF0000"
                  # Player name color
                  nameColor: "#FFFFFF"
        
                # How often to refresh the list, in seconds
                refreshIntervalSeconds: 10
                """;
        Files.writeString(path, defaultYaml);
    }

    private static String defaultAdminPrefix() {
        return "[OP] ";
    }
}
