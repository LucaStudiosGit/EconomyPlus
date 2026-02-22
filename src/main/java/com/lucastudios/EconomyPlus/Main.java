package com.lucastudios.EconomyPlus;

import com.hypixel.hytale.common.plugin.PluginIdentifier;
import com.hypixel.hytale.common.semver.SemverRange;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.command.system.CommandRegistry;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.Universe;
import com.lucastudios.EconomyPlus.Pages.TopBalPage;
import com.lucastudios.EconomyPlus.VaultUnlocked.VaultHook;
import com.lucastudios.EconomyPlus.VaultUnlocked.VaultUnlockedEconomy;
import com.lucastudios.EconomyPlus.api.EconomyAPI;
import com.lucastudios.EconomyPlus.commands.*;
import com.lucastudios.EconomyPlus.commands.Eco.EcoCommand;
import com.lucastudios.EconomyPlus.config.ConfigManager;
import com.lucastudios.EconomyPlus.config.CurrencyConfig;
import com.lucastudios.EconomyPlus.config.MessagesConfig;
import com.lucastudios.EconomyPlus.config.PluginConfig;
import com.lucastudios.EconomyPlus.hud.IBalanceProvider;
import com.lucastudios.EconomyPlus.hud.BalanceProvider;
import com.lucastudios.EconomyPlus.hud.WalletHudManager;
import com.lucastudios.EconomyPlus.model.Currency;
import com.lucastudios.EconomyPlus.model.Wallet;
import com.lucastudios.EconomyPlus.service.CurrencyRegistry;
import com.lucastudios.EconomyPlus.service.InMemoryEconomyService;
import com.lucastudios.EconomyPlus.service.JsonWalletStore;
import com.lucastudios.EconomyPlus.service.Messages;
import net.cfh.vault.VaultUnlockedServicesManager;

import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.*;

@SuppressWarnings("unused")
public final class Main extends JavaPlugin {

    private static Main instance;
    public Set<TopBalPage> openBaltopPages = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public static Main get() { return instance; }
    public WalletHudManager walletHudManager;
    private final HytaleLogger log;
    private final ConfigManager configManager = new ConfigManager();
    private final CurrencyRegistry currencyRegistry = new CurrencyRegistry();

    private PluginConfig config;
    private Messages messages;

    public JsonWalletStore walletStore;
    public InMemoryEconomyService economy;

    private ScheduledExecutorService autosave;
    private Path dataDir;

    public Main(JavaPluginInit init) {
        super(init);
        instance = this;
        this.log = getLogger();
    }

    public PluginConfig config() { return config; }
    public CurrencyRegistry currencies() { return currencyRegistry; }
    public InMemoryEconomyService economy() { return economy; }
    public Messages messages() { return messages; }

    @Override
    protected void setup() {
        try {
            dataDir = getDataDirectory();
            writeDefaults();
            reloadAll();
            registerCommands(getCommandRegistry());
            saveNow();
            startAutosave();
            EconomyAPI.setService(economy);
            getEventRegistry().register(PlayerConnectEvent.class, this::onPlayerConnect);

            log.atInfo().log("EconomyPlus loaded, currencies=" + currencyRegistry.keys());
            if (HytaleServer.get().getPluginManager().hasPlugin(
                    PluginIdentifier.fromString("TheNewEconomy:VaultUnlocked"),
                    SemverRange.WILDCARD
            )) {
                log.atInfo().log("VaultUnlocked is installed, enabling VaultUnlocked support.");
                VaultHook.initialize(this);
            } else {
                log.atInfo().log("VaultUnlocked is not installed, disabling VaultUnlocked support.");
            }
        } catch (Exception e) {
            log.atSevere().withCause(e).log("Failed to load EconomyPlus");
            throw new RuntimeException(e);
        }
    }

    private void onPlayerConnect(PlayerConnectEvent event) {
        economy.getOrCreateWallet(event.getPlayerRef().getUuid(), event.getPlayerRef().getUsername());
        log.atInfo().log("player connected: " + event.getPlayerRef().getUsername());
        walletHudManager.toggle(event.getPlayerRef());
    }
    @Override
    public void shutdown() {
        try {
            stopAutosave();
            saveNow();
            log.atInfo().log("EconomyPlus unloaded");
        } catch (Exception e) {
            log.atSevere().withCause(e).log("Unload failed");
        }
    }

    private void writeDefaults() throws Exception {
        writeIfMissing("config.yml");
        writeIfMissing("currencies.yml");
        writeIfMissing("messages.yml");
    }

    private void writeIfMissing(String filename) throws Exception {
        Path target = dataDir.resolve(filename);
        if (java.nio.file.Files.exists(target)) return;
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(filename)) {
            ConfigManager.writeDefaultResource(target, in);
        }
    }

    public synchronized void reloadAll() throws Exception {
        Path configFile = dataDir.resolve("config.yml");
        Path currenciesFile = dataDir.resolve("currencies.yml");
        Path messagesFile = dataDir.resolve("messages.yml");
        Path balancesFile = dataDir.resolve("balances.json");
        this.config = configManager.loadConfig(configFile);
        if (walletHudManager != null) walletHudManager.config = this.config;
        CurrencyConfig ccfg = configManager.loadCurrencies(currenciesFile);
        loadCurrencies(ccfg);

        MessagesConfig mcfg = configManager.loadMessages(messagesFile);
        if (this.messages == null) this.messages = new Messages(mcfg);
        else this.messages.reload(mcfg);

        this.walletStore = new JsonWalletStore();
        Path storagePath = dataDir.resolve(config.storage().file());
        Map<UUID, Wallet> loaded = walletStore.load(storagePath);
        this.economy = new InMemoryEconomyService(currencyRegistry, config, loaded, walletStore, storagePath);

        for (var p : Universe.get().getPlayers()) {
            economy.getOrCreateWallet(p.getUuid(), p.getUsername());
        }
    }

    private void loadCurrencies(CurrencyConfig cfg) {
        currencyRegistry.clear();
        for (var entry : cfg.currenciesNode().entrySet()) {
            String key = entry.getKey();
            if (!(entry.getValue() instanceof Map<?, ?> raw)) continue;

            @SuppressWarnings("unchecked")
            Map<String, Object> node = (Map<String, Object>) raw;

            String name = String.valueOf(node.getOrDefault("name", key));
            String symbol = String.valueOf(node.getOrDefault("symbol", ""));
            int decimals = node.get("decimals") instanceof Number n ? n.intValue() : 0;

            BigDecimal starting = BigDecimal.ZERO;
            Object sb = node.get("starting_balance");
            if (sb != null) {
                try { starting = new BigDecimal(String.valueOf(sb)); }
                catch (Exception ignored) {}
            }

            currencyRegistry.register(new Currency(key.toLowerCase(), name, symbol, decimals, starting));
        }
    }

    private void registerCommands(CommandRegistry registry) {
        registry.registerCommand(new BalCommand(this));
        registry.registerCommand(new PayCommand(this));
        registry.registerCommand(new BalTopCommand(this));
        registry.registerCommand(new EcoCommand(this));
        IBalanceProvider balanceProvider = new BalanceProvider(economy, currencyRegistry);
        walletHudManager = new WalletHudManager(
                balanceProvider,
                config,
                1000,
                "Wallet",
                50,
                50,
                50,
                50
        );
        registry.registerCommand(new WalletHudCommand(walletHudManager));
    }

    private void startAutosave() {
        int seconds = Math.max(10, config.storage().autosaveSeconds());
        autosave = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "EconomyPlus-Autosave");
            t.setDaemon(true);
            return t;
        });
        autosave.scheduleAtFixedRate(() -> {
            try { saveNow(); }
            catch (Exception e) { log.atSevere().withCause(e).log("Autosave failed"); }
        }, seconds, seconds, TimeUnit.SECONDS);
    }

    private void stopAutosave() {
        if (autosave == null) return;
        autosave.shutdownNow();
        autosave = null;
    }

    public synchronized void saveNow() throws Exception {
        if (walletStore == null || economy == null) return;
        walletStore.save(dataDir.resolve(config.storage().file()), economy.wallets());
    }
}
