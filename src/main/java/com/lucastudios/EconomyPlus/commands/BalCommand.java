package com.lucastudios.EconomyPlus.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.lucastudios.EconomyPlus.Main;
import com.lucastudios.EconomyPlus.model.Currency;
import com.lucastudios.EconomyPlus.model.Wallet;

import javax.annotation.Nonnull;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class BalCommand extends AbstractPlayerCommand {
    private final Main plugin;
    private final OptionalArg<String> targetPlayerArg;
    private final OptionalArg<String> currencyArg;

    public BalCommand(Main plugin) {
        super("bal", "Check your balance");
        this.plugin = plugin;

        targetPlayerArg = withOptionalArg("player", "Target player name", ArgTypes.STRING);
        currencyArg = withOptionalArg("currency", "Currency ID", ArgTypes.STRING);
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
            PlayerRef targetRef = findPlayer(targetPlayerName);
            if (targetRef == null) {
                ctx.sendMessage(Message.raw(plugin.messages().format("player_not_found")));
                return;
            }
            showBalance(ctx, targetRef, currencyId, false);
        } else {
            showBalance(ctx, playerRef, currencyId, true);
        }
    }

    private void showBalance(CommandContext ctx, PlayerRef target, String currencyId, boolean isSelf) {
        Wallet wallet = plugin.economy().getOrCreateWallet(target.getUuid(), target.getUsername());

        if (currencyId == null)
            currencyId = plugin.config().defaults().primaryCurrency();

        Currency currency = plugin.currencies().get(currencyId);
        if (currency == null) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("currency_id", currencyId);
            ctx.sendMessage(Message.raw(plugin.messages().format("currency_not_found", placeholders)));
            return;
        }

        long balance = wallet.getBalance(currencyId);
        String formatted = formatBalance(currency, balance);

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player", target.getUsername());
        placeholders.put("target", target.getUsername());
        placeholders.put("currency", currency.name());
        placeholders.put("currency_id", currency.currencyId());
        placeholders.put("symbol", currency.symbol());
        placeholders.put("balance", String.valueOf(balance));
        placeholders.put("balance_formatted", formatted);

        String messageKey = isSelf ? "balance_self_single" : "balance_other_single";
        ctx.sendMessage(Message.raw(plugin.messages().format(messageKey, placeholders)));
    }

    private String formatBalance(Currency currency, long balance) {
        if (currency.decimals() == 0) {
            NumberFormat nf = NumberFormat.getIntegerInstance(Locale.ENGLISH);
            return nf.format(balance);
        }
        return currency.format(balance);
    }

    private PlayerRef findPlayer(String name) {
        for (PlayerRef ref : Universe.get().getPlayers())
            if (ref.getUsername().equalsIgnoreCase(name))
                return ref;
        return null;
    }
}
