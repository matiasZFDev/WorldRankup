package com.worldplugins.rankup.config.data;

import me.post.lib.util.CollectionHelpers;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ShardsData {
    public static class Shard {
        private final byte id;
        private final @NotNull String name;
        private final @NotNull String display;
        private final double price;
        private final int limit;
        private final int defaultLimit;
        private final @NotNull ItemStack item;
        private final @NotNull ItemStack limitItem;

        public Shard(
            byte id,
            @NotNull String name,
            @NotNull String display,
            double price,
            int limit,
            int defaultLimit,
            @NotNull ItemStack item,
            @NotNull ItemStack limitItem
        ) {
            this.id = id;
            this.name = name;
            this.display = display;
            this.price = price;
            this.limit = limit;
            this.defaultLimit = defaultLimit;
            this.item = item;
            this.limitItem = limitItem;
        }

        public byte id() {
            return id;
        }

        public @NotNull String name() {
            return name;
        }

        public @NotNull String display() {
            return display;
        }

        public double price() {
            return price;
        }

        public int limit() {
            return limit;
        }

        public int defaultLimit() {
            return defaultLimit;
        }

        public @NotNull ItemStack item() {
            return item;
        }

        public @NotNull ItemStack limitItem() {
            return limitItem;
        }
    }

    private final @NotNull Map<Byte, Shard> shardsById;
    private final @NotNull Map<String, Shard> shardsByName;

    public ShardsData(@NotNull Collection<Shard> shards) {
        this.shardsById = shards.stream().collect(Collectors.toMap(Shard::id, Function.identity()));
        this.shardsByName = shards.stream().collect(Collectors.toMap(Shard::name, Function.identity()));
    }

    public Shard getById(byte id) {
        return shardsById.get(id);
    }

    public Shard getByName(@NotNull String name) {
        return shardsByName.get(name);
    }

    public @NotNull Collection<Shard> getAll() {
        return CollectionHelpers.immutable(shardsById.values());
    }
}
