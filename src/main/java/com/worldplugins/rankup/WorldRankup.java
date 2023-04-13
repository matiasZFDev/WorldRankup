package com.worldplugins.rankup;

import lombok.NonNull;
import org.bukkit.plugin.java.JavaPlugin;

public class WorldRankup extends JavaPlugin {
    private Runnable onDisable;

    public static final @NonNull Integer MAX_COMPARATIVE_SHARD_AMOUNT = 1000000000;

    @Override
    public void onEnable() {
        onDisable = new PluginExecutor(this).execute();
    }

    @Override
    public void onDisable() {
        if (onDisable != null)
            onDisable.run();
    }
}
