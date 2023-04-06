package com.worldplugins.rankup.init;

import com.worldplugins.lib.common.Initializer;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

@RequiredArgsConstructor
public class EconomyInitializer implements Initializer<Economy> {
    private final @NonNull Plugin plugin;

    @Override
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
