package com.worldplugins.rankup.config.data.shard;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public enum ShardSendType {
    PHYISIC((byte) 0, "fisico"), VIRTUAL((byte) 1, "virtual");

    private final byte id;
    private final @NotNull String name;

    ShardSendType(byte id, @NotNull String name) {
        this.id = id;
        this.name = name;
    }

    public byte id() {
        return id;
    }

    public static ShardSendType getByName(@NotNull String name) {
        return Arrays.stream(values())
            .filter(type -> type.name.equals(name))
            .findFirst()
            .orElseThrow(null);
    }
}
