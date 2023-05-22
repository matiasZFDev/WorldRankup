package com.worldplugins.rankup.config.data;

import com.worldplugins.rankup.config.data.prestige.Prestiges;
import org.jetbrains.annotations.NotNull;

public class PrestigeData {
    private final short defaulPrestige;
    private final @NotNull Prestiges prestiges;

    public PrestigeData(short defaulPrestige, @NotNull Prestiges prestiges) {
        this.defaulPrestige = defaulPrestige;
        this.prestiges = prestiges;
    }

    public short defaulPrestige() {
        return defaulPrestige;
    }

    public @NotNull Prestiges prestiges() {
        return prestiges;
    }
}
