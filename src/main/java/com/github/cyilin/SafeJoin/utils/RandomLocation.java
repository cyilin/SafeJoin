package com.github.cyilin.SafeJoin.utils;


import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Random;

public class RandomLocation {
    public static Location RandomLocation(Player player) {
        World world = player.getWorld();
        if (player.getLocation().getBlock().getBiome() == Biome.NETHER) {
            return null;
        }
        for (int i = 0; i < 10; i++) {
            Block block = getRandomLocation(player, 32);
            if (block != null) {
                Block b = world.getBlockAt(block.getX(), block.getY() - 1, block.getZ());
                if (block.getY() > 5) {
                    if (!b.isLiquid() && b.getType().isSolid()) {
                        return block.getLocation().add(new Vector(0.5D, 0.1D, 0.5D));
                    }
                }
            }
        }
        return null;
    }

    private static Block getRandomLocation(Player player, int radius) {
        World world = player.getWorld();
        int x = new Random().nextInt(radius) + 1;
        int z = new Random().nextInt(radius) + 1;
        if (new Random().nextBoolean()) {
            x *= -1;
        }
        if (new Random().nextBoolean()) {
            z *= -1;
        }
        return world.getHighestBlockAt((int) player.getLocation().getX() + x, (int) player.getLocation().getZ() + z);
    }
}
