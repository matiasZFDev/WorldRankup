package com.worldplugins.rankup.listener;

import br.com.devpaulo.legendchat.api.events.ChatMessageEvent;
import com.worldplugins.rankup.config.data.PrestigeData;
import com.worldplugins.rankup.config.data.RanksData;
import com.worldplugins.rankup.database.model.RankupPlayer;
import com.worldplugins.rankup.database.service.PlayerService;
import me.post.lib.config.model.ConfigModel;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public class PlayerTagChatListener implements Listener {
    private final @NotNull PlayerService playerService;
    private final @NotNull ConfigModel<RanksData> ranksConfig;
    private final @NotNull ConfigModel<PrestigeData> prestigeConfig;

    public PlayerTagChatListener(
        @NotNull PlayerService playerService,
        @NotNull ConfigModel<RanksData> ranksConfig,
        @NotNull ConfigModel<PrestigeData> prestigeConfig
    ) {
        this.playerService = playerService;
        this.ranksConfig = ranksConfig;
        this.prestigeConfig = prestigeConfig;
    }

    @EventHandler
    public void onChat(ChatMessageEvent event) {
        final RankupPlayer playerModel = playerService.getById(event.getSender().getUniqueId());

        if (playerModel == null) {
            return;
        }

        event.setTagValue("rank", ranksConfig.data().getById(playerModel.rank()).display());
        event.setTagValue(
            "prestigio",
            prestigeConfig.data().prestiges().getById(playerModel.prestige()).display()
        );
    }
}
