package com.lucastudios.EconomyPlus.hud;

import au.ellie.hyui.builders.*;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.lucastudios.EconomyPlus.config.PluginConfig;

import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public final class WalletHudManager {

    private final BalanceProvider balanceProvider;
    public PluginConfig config;
    private final HytaleLogger log = HytaleLogger.getLogger();

    // Per-player HUD instance
    private final Map<UUID, HyUIHud> active = new ConcurrentHashMap<>();

    // HUD tuning
    private final long refreshRateMs;
    private final String title;

    // Position and size
    private final int top;
    private final int right;
    private final int width;
    private final int height;

    public WalletHudManager(
            BalanceProvider balanceProvider,
            PluginConfig config,
            long refreshRateMs,
            String title,
            int top,
            int right,
            int width,
            int height
    ) {
        this.balanceProvider = Objects.requireNonNull(balanceProvider, "balanceProvider");
        this.config = config;
        this.refreshRateMs = Math.max(100, refreshRateMs);
        this.title = (title == null || title.isBlank()) ? "Wallet" : title;

        this.top = Math.max(0, top);
        this.right = Math.max(0, right);
        this.width = Math.max(150, width);
        this.height = Math.max(70, height);
    }

    public boolean isShown(PlayerRef playerRef) {
        return playerRef != null && active.containsKey(playerRef.getUuid());
    }

    public void toggle(PlayerRef playerRef) {
        if (playerRef == null) return;

        if (isShown(playerRef)) hide(playerRef);
        else show(playerRef);
    }

    public void show(PlayerRef playerRef) {
        if (playerRef == null) return;

        // If already shown, refresh config by re-showing
        hide(playerRef);

        // Build UI elements once, then mutate label text in onRefresh
        LabelBuilder walletText = LabelBuilder.label()
                .withId("WalletText")
                .withText("Loading...")
                .withPadding(HyUIPadding.all(6))
                .withStyle(new HyUIStyle()
                        .setFontSize(20)
                        .setWrap(true)
                        .setTextColor("#FFFFFF"));
        ContainerBuilder container = ContainerBuilder.decoratedContainer()
                .withId("WalletContainer")
                .withTitleText(title)
                .withAnchor(new HyUIAnchor().setFull(0))
                .addContentChild(walletText);

        GroupBuilder root = GroupBuilder.group()
                .withId("WalletHudRoot")
                .withAnchor(new HyUIAnchor()
                        .setTop(top)
                        .setRight(right)
                        .setWidth(width)
                        .setHeight(height))
                .addChild(walletText);

        HudBuilder builder = HudBuilder.hudForPlayer(playerRef)
                .withRefreshRate(refreshRateMs)
                .addElement(root)
                .onRefresh(hud -> {
                    // Update text each refresh
                    String rendered = renderBalances(playerRef);
                    hud.getById("WalletText", LabelBuilder.class).ifPresent(label -> label.withText(rendered));
                });

        HyUIHud hud = builder.show();
        active.put(playerRef.getUuid(), hud);
    }

    public void hide(PlayerRef playerRef) {
        if (playerRef == null) return;

        HyUIHud hud = active.remove(playerRef.getUuid());
        if (hud != null) {
            // Removes the HUD cleanly
            hud.remove();
        }
    }

    private String renderBalances(PlayerRef playerRef) {
        List<CurrencyBalance> balances;
        try {
            balances = balanceProvider.getBalances(playerRef);
        } catch (Throwable t) {
            return "Error loading wallet";
        }

        if (balances == null || balances.isEmpty()) {
            return "No currencies";
        }
        if (Objects.equals(config.hud().currency(), "primary"))
        {
            // Show only primary currency
            String primaryCurrencyId = config.defaults().primaryCurrency();
            for (CurrencyBalance b : balances) {
                if (b != null && b.displayName().equalsIgnoreCase(primaryCurrencyId)) {
                    NumberFormat nf = NumberFormat.getIntegerInstance(Locale.ENGLISH);
                    String name = safe(b.displayName());
                    String amount = nf.format(b.amount());
                    return name + ": " + amount;
                }
            }
            return "No primary currency";
        }

        NumberFormat nf = NumberFormat.getIntegerInstance(Locale.ENGLISH);
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < balances.size(); i++) {
            CurrencyBalance b = balances.get(i);
            if (b == null) continue;

            String name = safe(b.displayName());
//            String symbol = safe(b.symbol());
            String amount = nf.format(b.amount());

            // Example: Coins: $12,500
            sb.append(name).append(": ").append(amount);

            if (i < balances.size() - 1) sb.append("  ");
        }

        return sb.toString();
    }

    private static String safe(String s) {
        return (s == null) ? "" : s;
    }
}
