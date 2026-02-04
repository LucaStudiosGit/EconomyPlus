package com.lucastudios.EconomyPlus.commands.Eco;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.lucastudios.EconomyPlus.Main;
import com.lucastudios.EconomyPlus.commands.Eco.SubCommands.AddSubCommand;
import com.lucastudios.EconomyPlus.commands.Eco.SubCommands.ReloadSubCommand;
import com.lucastudios.EconomyPlus.commands.Eco.SubCommands.RemoveSubCommand;
import com.lucastudios.EconomyPlus.commands.Eco.SubCommands.SetSubCommand;

public final class EcoCommand extends AbstractCommandCollection {

    public EcoCommand(Main plugin) {
        super("eco", "Economy admin commands");
        addSubCommand(new SetSubCommand(plugin));
        addSubCommand(new AddSubCommand(plugin));
        addSubCommand(new RemoveSubCommand(plugin));
        addSubCommand(new ReloadSubCommand(plugin));
    }
}
