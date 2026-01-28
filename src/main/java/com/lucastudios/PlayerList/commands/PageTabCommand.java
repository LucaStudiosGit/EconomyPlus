package com.lucastudios.PlayerList.commands;

import com.lucastudios.PlayerList.Main;
import com.lucastudios.PlayerList.pages.TabPage;
import com.lucastudios.PlayerList.service.PlayerListService;
import com.lucastudios.PlayerList.service.ThreadUtil;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class PageTabCommand extends AbstractPlayerCommand {

    private final Main plugin;
    private final PlayerListService playerListService;
    public PageTabCommand(Main plugin, PlayerListService playerListService) {
        super("pl", "Opens the tab page", false);
        this.plugin = plugin;
        this.playerListService = playerListService;
        this.setPermissionGroup(GameMode.Adventure);
    }

    @Override
    protected void execute(
            @Nonnull CommandContext ctx,
            @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> ref,
            @Nonnull PlayerRef playerRef,
            @Nonnull World world
    ) {
        plugin.reloadTabYml();
        Player player = store.getComponent(ref, Player.getComponentType());
        assert player != null;
        CompletableFuture.runAsync(() -> {
            TabPage tabPage = new TabPage(playerRef, plugin, playerListService);
            plugin.openTabPages.add(tabPage);
            player.getPageManager().openCustomPage(ref, store, tabPage);
            tabPage.updateFuture = HytaleServer.SCHEDULED_EXECUTOR.scheduleAtFixedRate(() ->
            {
                try {
                    for (PlayerRef viewer : Universe.get().getPlayers()) {
                        ThreadUtil.runOnMainThread(viewer, ()
                                -> tabPage.refresh(plugin.getSortedOnlinePlayerNames()));
                    }
                }
                catch (Throwable t) {
                    LOGGER.atWarning().withCause(t).log("Error while refreshing tab page for player: " + tabPage.getPlayerRef().getUsername());
                }
            }, 1, playerListService.getRefreshIntervalSeconds(), TimeUnit.SECONDS);
        }, world);
    }
}