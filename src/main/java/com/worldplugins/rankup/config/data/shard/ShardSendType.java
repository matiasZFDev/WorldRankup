package com.worldplugins.rankup.config.data.shard;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
@Getter
public enum ShardSendType {
    PHYISIC((byte) 0, "fisico"), VIRTUAL((byte) 1, "virtual");

    private final byte id;
    private final @NonNull String name;

    public static ShardSendType getByName(@NonNull String name) {
        return Arrays.stream(values())
            .filter(type -> type.name.equals(name))
            .findFirst()
            .orElseThrow(null);
    }
}
