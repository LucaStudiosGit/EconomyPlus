package com.lucastudios.PlayerList;

import com.lucastudios.PlayerList.commands.PageTabCommand;
import com.lucastudios.PlayerList.pages.TabPage;
import com.lucastudios.PlayerList.service.PlayerListService;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

public class Main extends JavaPlugin {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private Path configPath;
    private final PlayerListService playerListService = new PlayerListService();
    private final Map<UUID, ScheduledFuture<?>> tabRefreshTasks = new ConcurrentHashMap<>();
    public final Set<TabPage> openTabPages = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public Main(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        super.setup();
        this.configPath = getDataDirectory().resolve("config.yml");
        playerListService.loadTabYml(getDataDirectory());
        reloadUiTextConfig(true);
        this.getCommandRegistry().registerCommand(new PageTabCommand(this, playerListService));
    }

    @Override
    public void shutdown() {
        super.shutdown();
        cancelAllTabRefreshTasks();
        LOGGER.atInfo().log(
                "Shutting down com.behar.TAB plugin"
        );

        for (PlayerRef ref : Universe.get().getPlayers()) {
            ref.getPacketHandler().disconnect("Server is shutting down");
            LOGGER.atInfo().log(
                    "Disconnected player " + ref.getUsername() + " due to server shutdown"
            );
        }
    }
    public void reloadUiTextConfig(boolean log) {
       playerListService.loadTabYml(getDataDirectory());
        if (log) {
            LOGGER.atInfo().log(
                    "com.behar.TAB config path: " + configPath.toAbsolutePath()
            );
        }
        // Refresh all open tabs with new config
//        for (TabPage tabPage : openTabPages) {
//            startAutoRefresh(tabPage);
//        }
    }

    public List<String> getSortedOnlinePlayerNames() {
        List<PlayerRef> players = Universe.get().getPlayers();
        List<String> names = new ArrayList<>(players.size());
        for (PlayerRef playerRef : players) {
            if (playerRef != null) {
                Player player = getPlayer(playerRef);
                if (player != null && player.hasPermission("OP"))
                    names.add(playerListService.getOpText() + playerRef.getUsername());
                else if(player != null)
                    names.add(playerListService.getDefaultText() + playerRef.getUsername());
            }
        }
        return playerListService.sortNames(names);
    }

    public Player getPlayer(PlayerRef playerRef) {
        if(playerRef == null)
        {
            return null;
        }
        Ref<EntityStore> ref = playerRef.getReference();
        if (ref == null || !ref.isValid()) {
            return null;
        }
        Store<EntityStore> store = ref.getStore();
//        assert playerRef.getWorldUuid() != null;
//        World world = Universe.get().getWorld(playerRef.getWorldUuid());
//        assert world != null;
//        final Player[] player = new Player[1];
//        if (world.isInThread()) {
//            return store.getComponent(ref, Player.getComponentType());
//        } else {
//            world.execute(() -> {
//                player[0] = store.getComponent(ref, Player.getComponentType());
//            });
//        }
//        return player[0];
        return store.getComponent(ref, Player.getComponentType());
    }


    public void reloadTabYml() {
        playerListService.loadTabYml(getDataDirectory());
        reloadUiTextConfig(false);
    }

    private void cancelAllTabRefreshTasks() {
        LOGGER.atInfo().log("Cancel all refresh tab page for player: ");
        for (ScheduledFuture<?> future : tabRefreshTasks.values()) {
            if (future != null) {
                future.cancel(false);
            }
        }
        tabRefreshTasks.clear();
        openTabPages.clear();
    }

    private ScheduledExecutorService getScheduler() {
        return HytaleServer.SCHEDULED_EXECUTOR;
    }
}
