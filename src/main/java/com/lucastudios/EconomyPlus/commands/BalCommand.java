package com.lucastudios.EconomyPlus.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.lucastudios.EconomyPlus.Main;
import com.lucastudios.EconomyPlus.model.Currency;
import com.lucastudios.EconomyPlus.model.Wallet;
import com.lucastudios.EconomyPlus.service.Utility;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public final class BalCommand extends AbstractPlayerCommand {
    private final Main plugin;
    private final OptionalArg<String> targetPlayerArg;
    private final OptionalArg<String> currencyArg;

    public BalCommand(Main plugin) {
        super("bal", "Check your balance");
        this.plugin = plugin;

        targetPlayerArg = withOptionalArg("p", "Target player name", ArgTypes.STRING);
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
        String targetPlayerName = ctx.provided(targetPlayerArg) ? ctx.get(targetPlayerArg) : null;
        String currencyId = ctx.provided(currencyArg) ? ctx.get(currencyArg) : null;

        if (targetPlayerName != null) {
            UUID targetUUID;
            try {
                targetUUID = Utility.GetUuidByPlayerName(plugin, targetPlayerName);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (targetUUID == null) {
                ctx.sendMessage(Message.raw(plugin.messages().format("player_not_found")));
                return;
            }
            showBalance(ctx, targetUUID, currencyId, targetPlayerName, false);
        } else {
            showBalance(ctx, playerRef.getUuid(), currencyId, playerRef.getUsername(), true);
        }
    }

    private void showBalance(CommandContext ctx, UUID targetUUID, String currencyId, String username, boolean isSelf) {
        Wallet wallet = plugin.economy().getOrCreateWallet(targetUUID, username);

        if (currencyId == null)
            currencyId = plugin.config().defaults().primaryCurrency();
        Currency currency = plugin.currencies().get(currencyId);
        if (currency == null) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("currency_id", currencyId);
            ctx.sendMessage(Message.raw(plugin.messages().format("currency_not_found", placeholders)));
            return;
        }

        BigDecimal balance = wallet.getBalance(currencyId);
        String formatted = formatBalance(currency, balance);

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player", username);
        placeholders.put("target", username);
        placeholders.put("currency", currency.name());
        placeholders.put("currency_id", currency.currencyId());
        placeholders.put("symbol", currency.symbol());
        placeholders.put("balance", String.valueOf(balance));
        placeholders.put("balance_formatted", formatted);

        String messageKey = isSelf ? "balance_self_single" : "balance_other_single";
        ctx.sendMessage(Message.raw(plugin.messages().format(messageKey, placeholders)));
    }

    private String formatBalance(Currency currency, BigDecimal balance) {
        if (currency.decimals() == 0) {
            NumberFormat nf = NumberFormat.getIntegerInstance(Locale.ENGLISH);
            return nf.format(balance);
        }
        return currency.format(balance);
    }
}
