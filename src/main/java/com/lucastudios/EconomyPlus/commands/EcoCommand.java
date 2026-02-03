package com.lucastudios.EconomyPlus.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgumentType;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.lucastudios.EconomyPlus.Main;
import com.lucastudios.EconomyPlus.model.Currency;
import com.lucastudios.EconomyPlus.model.TransactionResult;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class EcoCommand extends AbstractCommandCollection {
//    private final Main plugin;
//    private final RequiredArg<String> subcommandArg;
//    private final RequiredArg<String> targetPlayerArg;
//    private final RequiredArg<Double> amountArg;
//    private final RequiredArg<String> currencyArg;

    public EcoCommand(Main plugin) {
        super("eco", "Economy admin commands");
        addSubCommand(new SetSubCommand(plugin));
//        addSubCommand(new AddSubCommand());
//        addSubCommand(new RemoveSubCommand());

//        this.plugin = plugin;
//        this.setPermissionGroup(GameMode.Adventure);
//
//        this.subcommandArg = withRequiredArg("subcommand", "give, take, or set", ArgTypes.STRING);
//        this.targetPlayerArg = withRequiredArg("player", "Target player name", ArgTypes.STRING);
//        this.amountArg = withRequiredArg("amount", "Amount", ArgTypes.DOUBLE)
//            .addValidator(Validators.greaterThanOrEqualTo(0.0));
//        this.currencyArg = withRequiredArg("currency", "Currency ID", ArgTypes.STRING);
    }

//    @Override
//    protected void execute(
//            @Nonnull CommandContext ctx,
//            @Nonnull Store<EntityStore> store,
//            @Nonnull Ref<EntityStore> ref,
//            @Nonnull PlayerRef playerRef,
//            @Nonnull World world
//    ) {
//        Player player = store.getComponent(ref, Player.getComponentType());
//        if (player == null)
//            return;
//
//        String subcommand = subcommandArg.get(ctx).toLowerCase();
//        String targetName = targetPlayerArg.get(ctx);
//        double amountDouble = amountArg.get(ctx);
//        String currencyId = currencyArg.get(ctx);
//
//        PlayerRef targetRef = findPlayer(targetName);
//        if (targetRef == null) {
//            ctx.sendMessage(Message.raw(plugin.messages().format("player_not_found")));
//            return;
//        }
//
//        Currency currency = plugin.currencies().get(currencyId);
//        if (currency == null) {
//            Map<String, String> placeholders = new HashMap<>();
//            placeholders.put("currency_id", currencyId);
//            ctx.sendMessage(Message.raw(plugin.messages().format("currency_not_found", placeholders)));
//            return;
//        }
//
//        long amount = currency.toMinorUnits(new BigDecimal(amountDouble));
//        plugin.economy().getOrCreateWallet(targetRef.getUuid(), targetRef.getUsername());
//
//        TransactionResult result = switch (subcommand) {
//            case "give" -> plugin.economy().addBalance(targetRef.getUuid(), currencyId, amount);
//            case "take" -> plugin.economy().takeBalance(targetRef.getUuid(), currencyId, amount);
//            case "set" -> plugin.economy().setBalance(targetRef.getUuid(), currencyId, amount);
//            default -> {
//                ctx.sendMessage(Message.raw(plugin.messages().format("prefix") + "§cUnknown subcommand: " + subcommand));
//                yield null;
//            }
//        };
//
//        if (result == null)
//            return;
//
//        if (!result.isSuccess()) {
//            String messageKey = switch (((TransactionResult.Failure) result).reason()) {
//                case INSUFFICIENT_FUNDS -> "not_enough_balance";
//                case CURRENCY_NOT_FOUND -> "currency_not_found";
//                case PLAYER_NOT_FOUND -> "player_not_found";
//                default -> "invalid_amount";
//            };
//            ctx.sendMessage(Message.raw(plugin.messages().format(messageKey)));
//            return;
//        }
//
//        NumberFormat nf = NumberFormat.getIntegerInstance(Locale.ENGLISH);
//        Map<String, String> placeholders = new HashMap<>();
//        placeholders.put("target", targetRef.getUsername());
//        placeholders.put("currency", currency.name());
//        placeholders.put("symbol", currency.symbol());
//        placeholders.put("amount", String.valueOf(amount));
//        placeholders.put("amount_formatted", nf.format(amount));
//        placeholders.put("balance", String.valueOf(((TransactionResult.Success) result).newBalance()));
//        placeholders.put("balance_formatted", nf.format(((TransactionResult.Success) result).newBalance()));
//
//        String messageKey = "eco_" + subcommand + "_done";
//        ctx.sendMessage(Message.raw(plugin.messages().format(messageKey, placeholders)));
//    }

    private PlayerRef findPlayer(String name) {
        for (PlayerRef ref : Universe.get().getPlayers())
            if (ref.getUsername().equalsIgnoreCase(name))
                return ref;
        return null;
    }
}
