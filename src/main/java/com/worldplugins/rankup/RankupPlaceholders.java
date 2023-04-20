package com.worldplugins.rankup;

import com.worldplugins.lib.config.cache.ConfigCache;
import com.worldplugins.lib.extension.bukkit.ColorExtensions;
import com.worldplugins.rankup.config.data.PrestigeData;
import com.worldplugins.rankup.config.data.RanksData;
import com.worldplugins.rankup.database.model.RankupPlayer;
import com.worldplugins.rankup.database.service.PlayerService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

@ExtensionMethod({
    ColorExtensions.class
})

@RequiredArgsConstructor
public class RankupPlaceholders extends PlaceholderExpansion {
    private final @NonNull PlayerService playerService;
    private final @NonNull ConfigCache<RanksData> ranksConfig;
    private final @NonNull ConfigCache<PrestigeData> prestigeConfig;

    @Override
    public String getIdentifier() {
        return "worldrankup";
    }

    @Override
    public String getAuthor() {
        return "WorldPlugins";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public String onPlaceholderRequest(Player player, String params) {
        final RankupPlayer playerModel = playerService.getById(player.getUniqueId());

        if (playerModel == null) {
            switch (params) {
                case "rank":
                case "prestigio":
                    return "&c?".color();
            }
        }

        else {
            switch (params) {
                case "rank":
                    return ranksConfig.data().getById(playerModel.getRank()).getDisplay();

                case "prestigio":
                    return prestigeConfig.data().getPrestiges().getById(playerModel.getPrestige()).getDisplay();
            }
        }

        return null;
    }
}
