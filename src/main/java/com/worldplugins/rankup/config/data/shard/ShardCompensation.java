package com.worldplugins.rankup.config.data.shard;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public enum ShardCompensation {
    COMMAND("Comando"),
    BLOCK_BREAK("Quebrando-blocos"),
    MOB_HIT("Hitando-mobs"),
    MOB_KILL("Matando-mobs"),
    FISHING("Pescando");

    private final @NotNull String configName;

    ShardCompensation(@NotNull String configName) {
        this.configName = configName;
    }

    public @NotNull String configName() {
        return configName;
    }

    public static @Nullable ShardCompensation fromConfigName(@NotNull String configName) {
        return Arrays.stream(values())
            .filter(compensation -> compensation.configName().equals(configName))
            .findFirst()
            .orElse(null);
    }
}
