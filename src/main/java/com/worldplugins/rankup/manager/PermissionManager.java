package com.worldplugins.rankup.manager;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface PermissionManager {
    void addGroup(@NotNull Player player, @NotNull String group);
    void removeGroup(@NotNull Player player, @NotNull String group);
}
