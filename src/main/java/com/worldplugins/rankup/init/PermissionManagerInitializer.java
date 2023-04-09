package com.worldplugins.rankup.init;

import com.worldplugins.lib.common.Initializer;
import com.worldplugins.rankup.manager.PermissionManager;
import lombok.NonNull;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.node.Node;
import org.bukkit.entity.Player;

public class PermissionManagerInitializer implements Initializer<PermissionManager> {
    @Override
    public PermissionManager init() {
        return luckPerms();
    }

    private @NonNull PermissionManager luckPerms() {
        return new PermissionManager() {
            @Override
            public void addGroup(@NonNull Player player, @NonNull String group) {
                LuckPermsProvider.get().getUserManager().modifyUser(player.getUniqueId(), user -> {
                    user.data().add(Node.builder("group." + group).build());
                });
            }

            @Override
            public void removeGroup(@NonNull Player player, @NonNull String group) {
                LuckPermsProvider.get().getUserManager().modifyUser(player.getUniqueId(), user -> {
                    user.data().remove(Node.builder("group." + group).build());
                });
            }
        };
    }
}
