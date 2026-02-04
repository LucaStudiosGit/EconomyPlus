package com.lucastudios.EconomyPlus.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.NameMatching;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.lucastudios.EconomyPlus.Main;
import com.lucastudios.EconomyPlus.model.Currency;
import com.lucastudios.EconomyPlus.model.PayResult;
import com.lucastudios.EconomyPlus.service.Utility;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class PayCommand extends AbstractPlayerCommand {
    private final Main plugin;
    private final RequiredArg<String> targetPlayerArg;
    private final RequiredArg<Double> amountArg;
    private final OptionalArg<String> currencyArg;

    public PayCommand(Main plugin) {
        super("pay", "Pay another player");
        this.plugin = plugin;

        targetPlayerArg = withRequiredArg("player", "Target player name", ArgTypes.STRING);
        amountArg = withRequiredArg("amount", "Amount to pay", ArgTypes.DOUBLE);
        currencyArg = withOptionalArg("c", "Currency ID", ArgTypes.STRING);
    }

    @Override
    protected void execute(
            @Nonnull CommandContext ctx,
            @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> ref,
            @Nonnull PlayerRef playerRef,
            @Nonnull World world
    ) {
        String targetName = ctx.get(targetPlayerArg);
        Double amountDouble = ctx.get(amountArg);
        String currencyId = ctx.provided(currencyArg) ? ctx.get(currencyArg) : plugin.config().defaults().primaryCurrency();

        PlayerRef targetRef = findPlayer(targetName);
        if (targetRef == null) {
            ctx.sendMessage(Message.raw(plugin.messages().format("player_not_found")));
            return;
        }

        if (targetRef.getUuid().equals(playerRef.getUuid())) {
            ctx.sendMessage(Message.raw(plugin.messages().format("cannot_pay_self")));
            return;
        }

        Currency currency = plugin.currencies().get(currencyId);
        if (currency == null) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("currency_id", currencyId);
            ctx.sendMessage(Message.raw(plugin.messages().format("currency_not_found", placeholders)));
            return;
        }

        if (amountDouble <= 0) {
            ctx.sendMessage(Message.raw(plugin.messages().format("invalid_amount")));
            return;
        }

        long amount = currency.toMinorUnits(new BigDecimal(amountDouble));

        plugin.economy().getOrCreateWallet(playerRef.getUuid(), playerRef.getUsername());
        plugin.economy().getOrCreateWallet(targetRef.getUuid(), targetRef.getUsername());

        PayResult result = plugin.economy().pay(playerRef.getUuid(), targetRef.getUuid(), currencyId, amount);

        if (!result.success()) {
            String messageKey = switch (result.failureReason()) {
                case INSUFFICIENT_FUNDS -> "not_enough_balance";
                case CURRENCY_NOT_FOUND -> "currency_not_found";
                case PLAYER_NOT_FOUND -> "player_not_found";
                case INVALID_AMOUNT -> "invalid_amount";
                case CANNOT_PAY_SELF -> "cannot_pay_self";
            };
            ctx.sendMessage(Message.raw(plugin.messages().format(messageKey)));
            return;
        }

        NumberFormat nf = NumberFormat.getIntegerInstance(Locale.ENGLISH);

        Map<String, String> senderPlaceholders = new HashMap<>();
        senderPlaceholders.put("player", playerRef.getUsername());
        senderPlaceholders.put("target", targetRef.getUsername());
        senderPlaceholders.put("currency", currency.name());
        senderPlaceholders.put("symbol", currency.symbol());
        senderPlaceholders.put("amount", String.valueOf(result.gross()));
        senderPlaceholders.put("amount_formatted", nf.format(result.gross()));
        senderPlaceholders.put("tax", String.valueOf(result.tax()));
        senderPlaceholders.put("tax_formatted", nf.format(result.tax()));
        senderPlaceholders.put("net", String.valueOf(result.net()));
        senderPlaceholders.put("net_formatted", nf.format(result.net()));

        ctx.sendMessage(Message.raw(plugin.messages().format("pay_sent", senderPlaceholders)));

        if (result.tax() > 0)
            ctx.sendMessage(Message.raw(plugin.messages().format("pay_tax_info", senderPlaceholders)));

        Map<String, String> receiverPlaceholders = new HashMap<>();
        receiverPlaceholders.put("player", targetRef.getUsername());
        receiverPlaceholders.put("target", playerRef.getUsername());
        receiverPlaceholders.put("currency", currency.name());
        receiverPlaceholders.put("symbol", currency.symbol());
        receiverPlaceholders.put("amount", String.valueOf(result.net()));
        receiverPlaceholders.put("amount_formatted", nf.format(result.net()));
        receiverPlaceholders.put("net", String.valueOf(result.net()));
        receiverPlaceholders.put("net_formatted", nf.format(result.net()));

        sendMessageToPlayer(world, store, targetName, plugin.messages().format("pay_received", receiverPlaceholders));
    }

    private PlayerRef findPlayer(String name) {
        for (PlayerRef ref : Universe.get().getPlayers())
            if (ref.getUsername().equalsIgnoreCase(name))
                return ref;
        return null;
    }

    private void sendMessageToPlayer(World world, Store<EntityStore> senderStore, String playerName, String message) {
        world.execute(() -> {
            PlayerRef player = Universe.get().getPlayerByUsername(playerName, NameMatching.EXACT);
            if (player != null)
                player.sendMessage(Message.raw(message));
        });
    }
}
