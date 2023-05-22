package com.worldplugins.rankup.config.menu;

import com.worldplugins.lib.config.model.MenuModel;
import com.worldplugins.lib.config.model.menu.MenuData;
import com.worldplugins.lib.util.MenuModels;
import me.post.lib.config.wrapper.ConfigWrapper;
import me.post.lib.util.Items;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.util.HashMap;

public class RankupMenuModel implements MenuModel {
    private @UnknownNullability MenuData data;
    private final @NotNull ConfigWrapper configWrapper;

    public RankupMenuModel(@NotNull ConfigWrapper configWrapper) {
        this.configWrapper = configWrapper;
    }

    public void update() {
        data = MenuModels.fetch(configWrapper.unwrap())
            .modifyItems(items -> {
                Items.colorMeta(items.getById("Confirmar").item());
                Items.colorMeta(items.getById("Cancelar").item());
            })
            .getData(dataSection -> new HashMap<String, Object>() {{
                put("Status-suficiente", dataSection.getString("Status-suficiente"));
                put("Status-insuficiente", dataSection.getString("Status-insuficiente"));
                put("Formato-fragmento", dataSection.getString("Formato-fragmento"));
            }})
            .build();
    }

    @Override
    public @NotNull MenuData data() {
        return data;
    }
}
