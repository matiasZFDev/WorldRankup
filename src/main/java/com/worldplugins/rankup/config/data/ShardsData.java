package com.worldplugins.rankup.config.data;

import com.worldplugins.lib.extension.CollectionExtensions;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@ExtensionMethod({
    CollectionExtensions.class
})

public class ShardsData {
    @RequiredArgsConstructor
    @Getter
    public static class Shard {
        private final byte id;
        private final @NonNull String name;
        private final @NonNull String display;
        private final double price;
        private final int limit;
        private final int defaultLimit;
        private final @NonNull ItemStack item;
        private final @NonNull ItemStack limitItem;
    }

    private final @NonNull Map<Byte, Shard> shardsById;
    private final @NonNull Map<String, Shard> shardsByName;

    public ShardsData(@NonNull Collection<Shard> shards) {
        this.shardsById = shards.stream().collect(Collectors.toMap(Shard::getId, Function.identity()));
        this.shardsByName = shards.stream().collect(Collectors.toMap(Shard::getName, Function.identity()));
    }

    public Shard getById(byte id) {
        return shardsById.get(id);
    }

    public Shard getByName(@NonNull String name) {
        return shardsByName.get(name);
    }

    public @NonNull Collection<Shard> getAll() {
        return shardsById.values().immutable();
    }
}
