package com.lucastudios.EconomyPlus.commands;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.lucastudios.EconomyPlus.Main;

public final class EcoCommand extends AbstractCommandCollection {

    public EcoCommand(Main plugin) {
        super("eco", "Economy admin commands");
        addSubCommand(new SetSubCommand(plugin));
        addSubCommand(new AddSubCommand(plugin));
        addSubCommand(new RemoveSubCommand(plugin));
        addSubCommand(new ReloadSubCommand(plugin));
    }
}
