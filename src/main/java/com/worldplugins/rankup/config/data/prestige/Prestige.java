package com.worldplugins.rankup.config.data.prestige;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Prestige {
    private final short id;
    private final @NotNull String display;
    private final @NotNull String group;
    private final Short next;

    public Prestige(short id, @NotNull String display, @NotNull String group, Short next) {
        this.id = id;
        this.display = display;
        this.group = group;
        this.next = next;
    }

    public short id() {
        return id;
    }

    public @NotNull String display() {
        return display;
    }

    public @NotNull String group() {
        return group;
    }

    public @Nullable Short next() {
        return next;
    }
}
