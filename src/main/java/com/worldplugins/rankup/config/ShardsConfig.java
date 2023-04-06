package com.worldplugins.rankup.config;

import com.worldplugins.lib.common.Logger;
import com.worldplugins.lib.config.bukkit.ConfigContainer;
import com.worldplugins.lib.config.cache.StateConfig;
import com.worldplugins.lib.config.cache.annotation.Config;
import com.worldplugins.lib.extension.CollectionExtensions;
import com.worldplugins.lib.extension.bukkit.ConfigurationExtensions;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@ExtensionMethod({
    ConfigurationExtensions.class,
    CollectionExtensions.class
})

@Config(path = "fragmentos")
public class ShardsConfig extends StateConfig<ShardsConfig.Config> {
    public ShardsConfig(Logger logger, @NonNull ConfigContainer configContainer) {
        super(logger, configContainer);
    }

    public static class Config {
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

        private final @NonNull Map<Byte, Shard> shards;

        private Config(@NonNull Collection<Shard> shards) {
            this.shards = shards.stream().collect(Collectors.toMap(Shard::getId, Function.identity()));
        }

        public Shard getById(byte id) {
            return shards.get(id);
        }

        public @NonNull Optional<Shard> getByName(@NonNull String name) {
            return shards.values().stream().filter(shard -> shard.getName().equals(name)).findFirst();
        }

        public @NonNull Collection<Shard> getAll() {
            return shards.values().immutable();
        }
    }

    @Override
    public @NonNull Config fetch(@NonNull FileConfiguration config) {
        return new Config(
            config.map(section -> new Config.Shard(
                section.getByte("Id"),
                section.getString("Nome"),
                section.getString("Display"),
                section.numberFormat("Preco"),
                section.numberFormat("Limite").intValue(),
                section.numberFormat("Limite-padrao").intValue(),
                section.getItem("Iten", false),
                section.getItem("Iten-limite", false)
            ))
        );
    }
}