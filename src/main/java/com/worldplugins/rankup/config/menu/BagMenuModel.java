package com.worldplugins.rankup.config.menu;

import com.worldplugins.lib.config.model.MenuModel;
import com.worldplugins.lib.config.model.menu.MenuData;
import com.worldplugins.lib.util.ConfigSections;
import com.worldplugins.lib.util.MenuModels;
import me.post.lib.config.wrapper.ConfigWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.util.HashMap;

public class BagMenuModel implements MenuModel {
    private @UnknownNullability MenuData data;
    private final @NotNull ConfigWrapper configWrapper;

    public BagMenuModel(@NotNull ConfigWrapper configWrapper) {
        this.configWrapper = configWrapper;
    }

    public void update() {
        data = MenuModels.fetch(configWrapper.unwrap())
            .getData(dataSection ->
                new HashMap<String, Object>(){{
                    put("Slots", dataSection.getIntegerList("Slots"));
                    put("Display-fragmento", ConfigSections.itemDisplay(dataSection, "Display-fragmento"));
                }}
            )
            .build();
    }

    @Override
    public @NotNull MenuData data() {
        return data;
    }
}
