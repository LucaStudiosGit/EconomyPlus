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
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class RemoveSubCommand extends AbstractAsyncCommand {
    private final Main main;
    private final RequiredArg<String> playerArg;
    private final RequiredArg<Double> amountArg;
    private final OptionalArg<String> currencyArg;

    public RemoveSubCommand(Main main) {
        super("remove", "Remove currency from a player's balance");
        this.main = main;
        playerArg = withRequiredArg("player", "Player to remove balance", ArgTypes.STRING);
        amountArg = withRequiredArg("amount", "Amount to remove", ArgTypes.DOUBLE);
        currencyArg = withOptionalArg("c", "Currency ID", ArgTypes.STRING);
    }

    @Override
    @Nonnull
    protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {
        String playerName = context.get(playerArg);
        Double amountDouble = context.get(amountArg);
        String currencyId = context.provided(currencyArg) ? context.get(currencyArg) : main.config().defaults().primaryCurrency();

        UUID uuid;
        try {
            uuid = Utility.GetUuidByPlayerName(main, playerName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (uuid == null) {
            context.sendMessage(Message.raw("Player not found."));
            return CompletableFuture.completedFuture(null);
        }

        Currency currency = main.currencies().get(currencyId);
        if (currency == null) {
            context.sendMessage(Message.raw("Currency not found: " + currencyId));
            return CompletableFuture.completedFuture(null);
        }

        if (amountDouble <= 0) {
            context.sendMessage(Message.raw("Amount must be positive."));
            return CompletableFuture.completedFuture(null);
        }

        BigDecimal amount = currency.toMinorUnits(new BigDecimal(amountDouble));

        main.economy.getOrCreateWallet(uuid, playerName);
        TransactionResult result = EconomyAPI.withdraw(uuid, currencyId, amount);

        if (result instanceof TransactionResult.Failure(BigDecimal amount1, BigDecimal balance, TransactionResult.FailureReason reason, String message)) {
            String errorMessage = switch (reason) {
                case CURRENCY_NOT_FOUND -> "Currency not found: " + message;
                case PLAYER_NOT_FOUND -> "Player not found: " + message;
                case INSUFFICIENT_FUNDS -> "Insufficient funds. Player only has " + EconomyAPI.getBalance(uuid, currencyId) + " " + currency.name();
                case INVALID_AMOUNT -> "Invalid amount: " + message;
                default -> "An unknown error occurred: " + message;
            };
            context.sendMessage(Message.raw(errorMessage));
            return CompletableFuture.completedFuture(null);
        }

        if (!result.isSuccess()) {
            context.sendMessage(Message.raw("§cFailed to remove balance."));
            return CompletableFuture.completedFuture(null);
        }

        NumberFormat nf = NumberFormat.getIntegerInstance(Locale.ENGLISH);
        TransactionResult.Success success = (TransactionResult.Success) result;
        String formattedAmount = nf.format(amount);
        String formattedBalance = nf.format(success.newBalance().longValue());

        context.sendMessage(Message.raw(String.format(
            "Removed %s%s from %s's balance. New balance: %s%s",
            currency.symbol(),
            formattedAmount,
            playerName,
            currency.symbol(),
            formattedBalance
        )));

        return CompletableFuture.completedFuture(null);
    }
}
