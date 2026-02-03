package com.lucastudios.EconomyPlus.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.lucastudios.EconomyPlus.Main;

import javax.annotation.Nonnull;

public final class PayCommand extends AbstractPlayerCommand {
    private final Main plugin;

    public PayCommand(Main plugin) {
        super("pay", "Pay another player", false);
        this.plugin = plugin;
    }

    @Override
    protected void execute(
            @Nonnull CommandContext ctx,
            @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> ref,
            @Nonnull PlayerRef playerRef,
            @Nonnull World world
    ) {
        sendMessage(playerRef, plugin.messages().format("prefix") + "§cUsage: /pay <player> <amount> [currency]");
    }

    private void sendMessage(PlayerRef player, String message) {
        // TODO: Find correct Hytale API for sending chat messages
    }
}
