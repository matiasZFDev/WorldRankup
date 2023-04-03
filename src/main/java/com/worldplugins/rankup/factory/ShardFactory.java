package com.worldplugins.rankup.factory;

import lombok.NonNull;
import org.bukkit.inventory.ItemStack;

public interface ShardFactory {
    @NonNull ItemStack createShard(byte shardId, int amount);
    @NonNull ItemStack createLimit(byte shardId, int amount);
}
