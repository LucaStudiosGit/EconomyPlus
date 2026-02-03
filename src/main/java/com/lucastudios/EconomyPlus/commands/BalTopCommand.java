package com.lucastudios.EconomyPlus.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.lucastudios.EconomyPlus.Main;
import com.lucastudios.EconomyPlus.model.Currency;
import com.lucastudios.EconomyPlus.service.InMemoryEconomyService;

import javax.annotation.Nonnull;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class BalTopCommand extends AbstractPlayerCommand {
    private final Main plugin;

    public BalTopCommand(Main plugin) {
        super("baltop", "View top balances", false);
        this.plugin = plugin;
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
        String currencyId = plugin.config().defaults().primaryCurrency();
        int page = 1;

        Currency currency = plugin.currencies().get(currencyId);
        if (currency == null) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("currency_id", currencyId);
            sendMessage(playerRef, plugin.messages().format("currency_not_found", placeholders));
            return;
        }

        int entriesPerPage = plugin.config().baltop().entriesPerPage();
        List<InMemoryEconomyService.BalanceEntry> entries = plugin.economy().getBaltop(currencyId, page, entriesPerPage);

        if (entries.isEmpty()) {
            sendMessage(playerRef, plugin.messages().format("baltop_empty"));
            return;
        }

        Map<String, String> headerPlaceholders = new HashMap<>();
        headerPlaceholders.put("currency", currency.name());
        headerPlaceholders.put("page", String.valueOf(page));
        sendMessage(playerRef, plugin.messages().format("baltop_header", headerPlaceholders));

        NumberFormat nf = NumberFormat.getIntegerInstance(Locale.ENGLISH);
        int startRank = (page - 1) * entriesPerPage + 1;

        for (int i = 0; i < entries.size(); i++) {
            InMemoryEconomyService.BalanceEntry entry = entries.get(i);
            int rank = startRank + i;

            Map<String, String> linePlaceholders = new HashMap<>();
            linePlaceholders.put("rank", String.valueOf(rank));
            linePlaceholders.put("player", entry.playerName());
            linePlaceholders.put("balance", String.valueOf(entry.balance()));
            linePlaceholders.put("balance_formatted", nf.format(entry.balance()));
            linePlaceholders.put("symbol", currency.symbol());
            linePlaceholders.put("currency", currency.name());

            sendMessage(playerRef, plugin.messages().format("baltop_line", linePlaceholders));
        }
    }

    private void sendMessage(PlayerRef player, String message) {
        // TODO: Find correct Hytale API for sending chat messages
    }
}
