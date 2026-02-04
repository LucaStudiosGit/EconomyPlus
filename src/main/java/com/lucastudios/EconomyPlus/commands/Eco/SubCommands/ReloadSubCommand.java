package com.lucastudios.EconomyPlus.commands.Eco.SubCommands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.lucastudios.EconomyPlus.Main;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

public class ReloadSubCommand extends AbstractAsyncCommand {
    private final Main main;

    public ReloadSubCommand(Main main) {
        super("reload", "Reloads all configuration files");
        this.main = main;
    }

    @Override
    @Nonnull
    protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {
        try {
            context.sendMessage(Message.raw("Reloading EconomyPlus configuration..."));
            main.reloadAll();
            context.sendMessage(Message.raw("EconomyPlus configuration reloaded successfully!"));
            context.sendMessage(Message.raw("Currencies: " + main.currencies().keys()));
        } catch (Exception e) {
            context.sendMessage(Message.raw("Failed to reload configuration: " + e.getMessage()));
            main.getLogger().atSevere().withCause(e).log("Failed to reload configuration");
        }
        return CompletableFuture.completedFuture(null);
    }
}
