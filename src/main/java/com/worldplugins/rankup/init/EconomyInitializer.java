package com.worldplugins.rankup.init;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;

public class EconomyInitializer {
    private final @NotNull Plugin plugin;

    public EconomyInitializer(@NotNull Plugin plugin) {
        this.plugin = plugin;
    }

    public Economy init() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            return null;
        }

        final RegisteredServiceProvider<Economy> rsp = plugin.getServer()
            .getServicesManager()
            .getRegistration(Economy.class);

        if (rsp == null) {
            return null;
        }

        return rsp.getProvider();
    }
}
