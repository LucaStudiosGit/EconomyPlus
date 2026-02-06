package com.lucastudios.EconomyPlus.Pages;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.lucastudios.EconomyPlus.commands.BalTopCommand;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

public class TopBalPage extends InteractiveCustomUIPage<TopBalPage.SearchGuiData> {

    public final PlayerRef player;
    private final List<String> playerList;
    private final BalTopCommand balTopCommand;
    private String searchQuery = "";
    private final List<String> visibleNames = new ObjectArrayList<>();

    public TopBalPage(Ref<EntityStore> ref, Store<EntityStore> store, @Nonnull PlayerRef player, List<String> playerList, BalTopCommand balTopCommand) {
        super(player, CustomPageLifetime.CanDismiss, SearchGuiData.CODEC);
        this.balTopCommand = balTopCommand;
        this.player = player;
        this.playerList = playerList;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder uiCommandBuilder, @Nonnull UIEventBuilder uiEventBuilder, @Nonnull Store<EntityStore> store) {
        uiCommandBuilder.append("Pages/TopBalance.ui");
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#NextPageButton", EventData.of("Button", "NextPageButton"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#PrevPageButton", EventData.of("Button", "PrevPageButton"), false);
        uiCommandBuilder.set("#SearchInput.Value", this.searchQuery);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#SearchInput", EventData.of("@SearchQuery", "#SearchInput.Value"), false);
        this.buildList(ref, uiCommandBuilder, uiEventBuilder, store);
    }

    @Override
    public void onDismiss(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
        super.onDismiss(ref, store);
    }

    public static class SearchGuiData {
        static final String KEY_ITEM = "Item";
        static final String KEY_SEARCH_QUERY = "@SearchQuery";
        static final String KEY_BUTTON = "Button";
        public static final BuilderCodec<SearchGuiData> CODEC = BuilderCodec.<SearchGuiData>builder(SearchGuiData.class, SearchGuiData::new)
                .addField(new KeyedCodec<>(KEY_SEARCH_QUERY, Codec.STRING), (searchGuiData, s) -> searchGuiData.searchQuery = s, searchGuiData -> searchGuiData.searchQuery)
                .addField(new KeyedCodec<>(KEY_ITEM, Codec.STRING), (searchGuiData, s) -> searchGuiData.item = s, searchGuiData -> searchGuiData.item)
                .addField(new KeyedCodec<>(KEY_BUTTON, Codec.STRING), (searchGuiData, s) -> searchGuiData.button = s, searchGuiData -> searchGuiData.button).build();

        private String item;
        private String searchQuery;
        private String button;

    }

    public PlayerRef getPlayerRef() {
        return player;
    }

    private void buildList(Ref<EntityStore> ref, UICommandBuilder uiCommandBuilder, UIEventBuilder uiEventBuilder, Store<EntityStore> store) {
        this.visibleNames.clear();
        if (this.searchQuery.isEmpty()) {
            this.visibleNames.addAll(playerList);
        } else {
            String lowerCaseQuery = this.searchQuery.toLowerCase(Locale.ENGLISH);
            for (String name : playerList) {
                if (name.toLowerCase(Locale.ENGLISH).contains(lowerCaseQuery)) {
                    this.visibleNames.add(name);
                }
            }
        }
        this.refreshInto(uiCommandBuilder, this.visibleNames);
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull SearchGuiData data) {
        super.handleDataEvent(ref, store, data);
        if (data.button != null) {
            if (data.button.equals("NextPageButton")){
                System.out.println("Next page button clicked!");
            }

            else if (data.button.equals("PrevPageButton"))
                System.out.println("Previous page button clicked!");
        }
        if (data.item != null) {
            this.sendUpdate();
        }
        if (data.searchQuery != null) {
            this.searchQuery = data.searchQuery.trim().toLowerCase();
            UICommandBuilder commandBuilder = new UICommandBuilder();
            UIEventBuilder eventBuilder = new UIEventBuilder();
            this.buildList(ref, commandBuilder, eventBuilder, store);
            this.sendUpdate(commandBuilder, eventBuilder, false);
        }
    }

    public String render(List<String> lines) {
        return lines.stream()
                .map(this::applyPlaceholders)
                .collect(Collectors.joining("\n"));
    }

    private String applyPlaceholders(String line) {
        if (line == null) return "";

        return line
                .replace("%online%", String.valueOf(Universe.get().getPlayerCount()))
                .replace("%player%", player.getUsername());
    }


    public void refresh(List<String> sortedNames) {
        UICommandBuilder commands = new UICommandBuilder();

        refreshInto(commands, sortedNames);

        sendUpdate(commands, false);
    }


    public void refreshInto(UICommandBuilder commands, List<String> sortedNames) {
        commands.clear("#PlayersList");

        if (sortedNames == null || sortedNames.isEmpty()) {
            commands.appendInline("#PlayersList",
                    "Label { Text: \"No players found\"; Anchor: (Width: 600, Height: 30); Style: (Alignment: Center); }");
            return;
        }

        int columnWidth = 320;
        int rowHeight = 35;
        int maxRowsPerColumn = 14;

        for (int i = 0; i < sortedNames.size(); i++) {
            String raw = escape(sortedNames.get(i));
            int col = i / maxRowsPerColumn;
            int row = i % maxRowsPerColumn;

            int xPos = col * columnWidth;
            int yPos = row * rowHeight;

            if (col < 3) {
                String labelMarkup = String.format(
                        "Label { " +
                                "Text: \"%s\"; " +
                                "Anchor: (Top: %d, Left: %d, Width: %d, Height: %d); " +
                                "Style: (FontSize: 22); " +
                                "}",
                        playerList.get(i), yPos, xPos, columnWidth, rowHeight//, tagColor //isOP(raw) ? plugin.getOpTagColor() : plugin.getDefaultTagColor()
                );
//                String labelMarkup1 = String.format(
//                        "Label { " +
//                                "Text: \"%s\"; " +
//                                "Anchor: (Top: %d, Left: %d, Width: %d, Height: %d); " +
//                                "Style: (FontSize: 22, TextColor: %s); " +
//                                "}",
//                        isOP(raw) ? playerListService.getOpText() : playerListService.getDefaultText(), yPos, xPos, columnWidth, rowHeight, tagColor //isOP(raw) ? plugin.getOpTagColor() : plugin.getDefaultTagColor()
//                );
//
//                String labelMarkup2 = String.format(
//                        "Label { " +
//                                "Text: \"%s\"; " +
//                                "Anchor: (Top: %d, Left: %d, Width: %d, Height: %d); " +
//                                "Style: (FontSize: 22, TextColor: %s); " +
//                                "}",
//                        raw, yPos, xPos, columnWidth, rowHeight, nameColor //isOP(raw) ? plugin.getOpTagColor() : plugin.getDefaultTagColor()
//                );
//
//                commands.appendInline("#PlayersList", labelMarkup2);
                commands.appendInline("#PlayersList", labelMarkup);
            }
        }
    }
    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
//    private boolean isOP(String name) {
//        String prefix = playerListService.getOpText();
//        return name.startsWith(prefix);
//    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TopBalPage topBalPage = (TopBalPage) o;
        return Objects.equals(player, topBalPage.player);
    }

    @Override
    public int hashCode() {
        return Objects.hash(player);
    }
}
