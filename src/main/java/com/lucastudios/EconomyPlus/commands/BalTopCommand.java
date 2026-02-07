package com.lucastudios.EconomyPlus.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.lucastudios.EconomyPlus.Main;
import com.lucastudios.EconomyPlus.Pages.TopBalPage;
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
    private final OptionalArg<String> currencyArg;
    private final OptionalArg<Integer> pageArg;

    private Store<EntityStore> currentStore;
    private Ref<EntityStore> currentRef;
    private PlayerRef currentPlayerRef;
    private int currentPage = 1;
    private String currentCurrencyId;

    public BalTopCommand(Main plugin) {
        super("baltop", "View top balances");
        this.plugin = plugin;
        this.setPermissionGroup(GameMode.Adventure);

        currencyArg = withOptionalArg("c", "Currency ID", ArgTypes.STRING);
        pageArg = withOptionalArg("p", "Page number", ArgTypes.INTEGER);
    }

    @Override
    protected void execute(
            @Nonnull CommandContext ctx,
            @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> ref,
            @Nonnull PlayerRef playerRef,
            @Nonnull World world
    ) {
        String currencyId = ctx.provided(currencyArg) ? ctx.get(currencyArg) : plugin.config().defaults().primaryCurrency();
        int page = ctx.provided(pageArg) ? ctx.get(pageArg) : 1;

        this.currentStore = store;
        this.currentRef = ref;
        this.currentPlayerRef = playerRef;
        this.currentPage = page;
        this.currentCurrencyId = currencyId;

        Currency currency = plugin.currencies().get(currencyId);
        if (currency == null) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("currency_id", currencyId);
            ctx.sendMessage(Message.raw(plugin.messages().format("currency_not_found", placeholders)));
            return;
        }

        int entriesPerPage = plugin.config().baltop().entriesPerPage();
        List<InMemoryEconomyService.BalanceEntry> entries = plugin.economy().getBaltop(currencyId, page, entriesPerPage);
        List<String> playerList = new java.util.ArrayList<>(List.of());
        if (entries.isEmpty()) {
            ctx.sendMessage(Message.raw(plugin.messages().format("baltop_empty")));
            return;
        }

        Map<String, String> headerPlaceholders = new HashMap<>();
        Map<String, String> bottomPlaceholders = new HashMap<>();

        bottomPlaceholders.put("page", String.valueOf(page));

        headerPlaceholders.put("currency", currency.name());
        headerPlaceholders.put("page", String.valueOf(page));
        ctx.sendMessage(Message.raw(plugin.messages().format("baltop_header", headerPlaceholders)));
        CreatePage(store, ref, playerRef, page, entriesPerPage, entries, currency, playerList, headerPlaceholders);
    }

    public void CreatePage(Store<EntityStore> store, Ref<EntityStore> ref, PlayerRef playerRef, int page, int entriesPerPage, List<InMemoryEconomyService.BalanceEntry> entries, Currency currency, List<String> playerList, Map<String, String> headerPlaceholders) {
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
            playerList.add(plugin.messages().format("baltop_line", linePlaceholders));
        }
        Player player = store.getComponent(ref, Player.getComponentType());
        TopBalPage topBalPage = new TopBalPage(ref, store, playerRef, playerList, this);
        plugin.openBaltopPages.add(topBalPage);
        assert player != null;
        player.getPageManager().openCustomPage(ref, store, topBalPage);
    }

    public void nextPage() {
        currentPage++;

        Currency currency = plugin.currencies().get(currentCurrencyId);
        int entriesPerPage = plugin.config().baltop().entriesPerPage();
        List<InMemoryEconomyService.BalanceEntry> entries = plugin.economy().getBaltop(currentCurrencyId, currentPage, entriesPerPage);
        List<String> playerList = new java.util.ArrayList<>();

        if (entries.isEmpty()) {
            currentPage--;
            return;
        }

        Map<String, String> headerPlaceholders = new HashMap<>();
        headerPlaceholders.put("currency", currency.name());
        headerPlaceholders.put("page", String.valueOf(currentPage));

        CreatePage(currentStore, currentRef, currentPlayerRef, currentPage, entriesPerPage, entries, currency, playerList, headerPlaceholders);
    }

    public void prevPage() {
        if (currentPage > 1) {
            currentPage--;

            Currency currency = plugin.currencies().get(currentCurrencyId);
            int entriesPerPage = plugin.config().baltop().entriesPerPage();
            List<InMemoryEconomyService.BalanceEntry> entries = plugin.economy().getBaltop(currentCurrencyId, currentPage, entriesPerPage);
            List<String> playerList = new java.util.ArrayList<>();

            Map<String, String> headerPlaceholders = new HashMap<>();
            headerPlaceholders.put("currency", currency.name());
            headerPlaceholders.put("page", String.valueOf(currentPage));

            CreatePage(currentStore, currentRef, currentPlayerRef, currentPage, entriesPerPage, entries, currency, playerList, headerPlaceholders);
        }
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getTotalPages() {
        return plugin.economy().getBaltopTotalPages(currentCurrencyId, plugin.config().baltop().entriesPerPage());
    }
}
