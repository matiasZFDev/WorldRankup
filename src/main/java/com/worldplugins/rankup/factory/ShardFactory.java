package com.worldplugins.rankup.factory;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface ShardFactory {
    @NotNull ItemStack createShard(byte shardId, int amount);
    @NotNull ItemStack createLimit(byte shardId, int amount);
}
