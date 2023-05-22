package com.worldplugins.rankup;

import com.worldplugins.rankup.config.data.PrestigeData;
import com.worldplugins.rankup.config.data.RanksData;
import com.worldplugins.rankup.database.model.RankupPlayer;
import com.worldplugins.rankup.database.service.PlayerService;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.post.lib.config.model.ConfigModel;
import me.post.lib.util.Colors;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class RankupPlaceholders extends PlaceholderExpansion {
    private final @NotNull PlayerService playerService;
    private final @NotNull ConfigModel<RanksData> ranksConfig;
    private final @NotNull ConfigModel<PrestigeData> prestigeConfig;

    public RankupPlaceholders(@NotNull PlayerService playerService, @NotNull ConfigModel<RanksData> ranksConfig, @NotNull ConfigModel<PrestigeData> prestigeConfig) {
        this.playerService = playerService;
        this.ranksConfig = ranksConfig;
        this.prestigeConfig = prestigeConfig;
    }

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
                    return Colors.color("&c?");
            }
        }

        else {
            switch (params) {
                case "rank":
                    return ranksConfig.data().getById(playerModel.rank()).display();

                case "prestigio":
                    return prestigeConfig.data().prestiges().getById(playerModel.prestige()).display();
            }
        }

        return null;
    }
}
