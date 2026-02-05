package com.lucastudios.EconomyPlus.commands.Eco.SubCommands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.lucastudios.EconomyPlus.Main;
import com.lucastudios.EconomyPlus.api.EconomyAPI;
import com.lucastudios.EconomyPlus.model.Currency;
import com.lucastudios.EconomyPlus.model.TransactionResult;
import com.lucastudios.EconomyPlus.service.Utility;


import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SetSubCommand extends AbstractAsyncCommand {
    private final Main main;
    private final RequiredArg<String> playerArg;
    private final RequiredArg<Double> amountArg;
    private final OptionalArg<String> currencyArg;
    public SetSubCommand(Main main) {
        this.main = main;
        super("set", "Set a player's balance", false);
        playerArg = withRequiredArg("player", "Player to set balance", ArgTypes.STRING);
        amountArg = withRequiredArg("amount", "Amount to set", ArgTypes.DOUBLE);
        currencyArg = withOptionalArg("c", "Currency ID", ArgTypes.STRING);
    }

    @Override
    @Nonnull
    protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {
        String player = context.get(playerArg);
        Double amount = context.get(amountArg);
        String currency = context.provided(currencyArg) ? context.get(currencyArg) : main.config().defaults().primaryCurrency();
        UUID uuid;
        try {
            uuid = Utility.GetUuidByPlayerName(main, player);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (uuid == null) {
            context.sendMessage(Message.raw("Player not found."));
            return CompletableFuture.completedFuture(null);
        }
        main.economy.getOrCreateWallet(uuid, player);
        TransactionResult result = EconomyAPI.setBalance(uuid, currency, amount);
        if (result instanceof TransactionResult.Failure failure) {
            String errorMessage = switch (failure.reason()) {
                case CURRENCY_NOT_FOUND -> "Currency not found." + failure.message();
                case PLAYER_NOT_FOUND -> "Player not found." + failure.message();
                case INVALID_AMOUNT -> "Invalid amount." + failure.message();
                default -> "An unknown error occurred." + failure.message();
            };
            context.sendMessage(Message.raw(errorMessage));
            return CompletableFuture.completedFuture(null);
        }
        if (!result.isSuccess()) {
            context.sendMessage(Message.raw("Failed to set balance: "));
            return CompletableFuture.completedFuture(null);
        }
        context.sendMessage(Message.raw(String.format(
                "Set %s's balance to %s %s",
                player,
                amount,
                currency
        )));
        return CompletableFuture.completedFuture(null);
    }
}
