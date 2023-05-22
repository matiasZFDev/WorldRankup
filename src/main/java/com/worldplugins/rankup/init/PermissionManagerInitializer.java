package com.worldplugins.rankup.init;

import com.worldplugins.rankup.manager.PermissionManager;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.node.Node;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PermissionManagerInitializer {
    public PermissionManager init() {
        return luckPerms();
    }

    private @NotNull PermissionManager luckPerms() {
        return new PermissionManager() {
            @Override
            public void addGroup(@NotNull Player player, @NotNull String group) {
                LuckPermsProvider.get().getUserManager().modifyUser(player.getUniqueId(), user -> {
                    user.data().add(Node.builder("group." + group).build());
                });
            }

            @Override
            public void removeGroup(@NotNull Player player, @NotNull String group) {
                LuckPermsProvider.get().getUserManager().modifyUser(player.getUniqueId(), user -> {
                    user.data().remove(Node.builder("group." + group).build());
                });
            }
        };
    }
}
