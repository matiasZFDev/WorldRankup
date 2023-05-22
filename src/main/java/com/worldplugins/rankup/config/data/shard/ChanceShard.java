package com.worldplugins.rankup.config.data.shard;

import org.jetbrains.annotations.NotNull;

public class ChanceShard {
    private final @NotNull String name;
    private final double chance;
    private final int amount;

    public ChanceShard(@NotNull String name, double chance, int amount) {
        this.name = name;
        this.chance = chance;
        this.amount = amount;
    }

    public @NotNull String name() {
        return name;
    }

    public double chance() {
        return chance;
    }

    public int amount() {
        return amount;
    }
}
