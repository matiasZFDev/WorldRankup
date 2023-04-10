package com.worldplugins.rankup.util;

import lombok.NonNull;
import org.bukkit.Bukkit;

public class BukkitUtils {
    public static void consoleCommand(@NonNull String command) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }
}
