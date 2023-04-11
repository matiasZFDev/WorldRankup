package com.worldplugins.rankup;

import com.worldplugins.lib.extension.bukkit.ColorExtensions;
import com.worldplugins.rankup.config.PrestigeConfig;
import com.worldplugins.rankup.config.RanksConfig;
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
    private final @NonNull RanksConfig ranksConfig;
    private final @NonNull PrestigeConfig prestigeConfig;

    @Override
    public String getIdentifier() {
        return "worldplugins";
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
                case "worldrankup_rank":
                case "worldrankup_prestigio":
                    return "&c?".color();
            }
        }

        else {
            switch (params) {
                case "worldrankup_rank":
                    return ranksConfig.get().getById(playerModel.getRank()).getDisplay();

                case "worldrankup_prestigio":
                    return prestigeConfig.get().getPrestiges().getById(playerModel.getPrestige()).getDisplay();
            }
        }

        return null;
    }
}
