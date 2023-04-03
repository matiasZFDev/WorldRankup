package com.worldplugins.rankup.config.data;

import lombok.Getter;
import lombok.NonNull;

import java.util.Arrays;
import java.util.Optional;

public enum ShardCompensation {
    COMMAND("Comando"),
    BLOCK_BREAK("Quebrando-blocos"),
    MOB_HIT("Hitando-mobs"),
    MOB_KILL("Matando-mobs"),
    FISHING("Pescando");

    @Getter
    private final @NonNull String configName;

    ShardCompensation(@NonNull String configName) {
        this.configName = configName;
    }

    public static @NonNull Optional<ShardCompensation> fromConfigName(@NonNull String configName) {
        return Arrays.stream(values())
            .filter(compensation -> compensation.getConfigName().equals(configName))
            .findFirst();
    }
}
