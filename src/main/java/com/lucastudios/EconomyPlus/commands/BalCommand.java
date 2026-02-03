package com.lucastudios.EconomyPlus.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
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

    public BalCommand(Main plugin) {
        super("bal", "Check your balance", false);
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
        String targetPlayerName = null;
        String currencyId = null;

        if (targetPlayerName != null) {
            PlayerRef targetRef = findPlayer(targetPlayerName);
            if (targetRef == null) {
                sendMessage(playerRef, plugin.messages().format("player_not_found"));
                return;
            }
            showBalance(playerRef, targetRef, currencyId, false);
        } else {
            showBalance(playerRef, playerRef, currencyId, true);
        }
    }

    private void showBalance(PlayerRef sender, PlayerRef target, String currencyId, boolean isSelf) {
        Wallet wallet = plugin.economy().getOrCreateWallet(target.getUuid(), target.getUsername());

        if (currencyId == null)
            currencyId = plugin.config().defaults().primaryCurrency();

        Currency currency = plugin.currencies().get(currencyId);
        if (currency == null) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("currency_id", currencyId);
            sendMessage(sender, plugin.messages().format("currency_not_found", placeholders));
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
        sendMessage(sender, plugin.messages().format(messageKey, placeholders));
    }

    private String formatBalance(Currency currency, long balance) {
        if (currency.decimals() == 0) {
            NumberFormat nf = NumberFormat.getIntegerInstance(Locale.ENGLISH);
            return nf.format(balance);
        }
        return currency.format(balance);
    }

    private PlayerRef findPlayer(String name) {
        for (PlayerRef ref : Universe.get().getPlayers()) {
            if (ref.getUsername().equalsIgnoreCase(name))
                return ref;
        }
        return null;
    }

    private void sendMessage(PlayerRef player, String message) {
        // TODO: Find correct Hytale API for sending chat messages
        // Possible options: player.sendSystemMessage(), ctx.sendFeedback(), etc.
    }
}
