package com.lucastudios.EconomyPlus.service;

import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;

public final class ThreadUtil {

    public static void runOnMainThread(PlayerRef player, Runnable task) {
        assert player.getWorldUuid() != null;
        World world = Universe.get().getWorld(player.getWorldUuid());
        if (world == null) return;

        // If we're already on the world thread, run now, otherwise queue for next tick
        if (world.isInThread()) {
            task.run();
        } else {
            world.execute(task);
        }
    }

    public static void runOnMainThread(World world, Runnable task) {
        if (world == null) return;

        if (world.isInThread()) task.run();
        else world.execute(task);
    }
}
