package com.worldplugins.rankup.listener;

import br.com.devpaulo.legendchat.api.events.ChatMessageEvent;
import com.worldplugins.lib.config.cache.ConfigCache;
import com.worldplugins.rankup.config.data.PrestigeData;
import com.worldplugins.rankup.config.data.RanksData;
import com.worldplugins.rankup.database.model.RankupPlayer;
import com.worldplugins.rankup.database.service.PlayerService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

@RequiredArgsConstructor
public class PlayerTagChatListener implements Listener {
    private final @NonNull PlayerService playerService;
    private final @NonNull ConfigCache<RanksData> ranksConfig;
    private final @NonNull ConfigCache<PrestigeData> prestigeConfig;

    @EventHandler
    public void onChat(ChatMessageEvent event) {
        final RankupPlayer playerModel = playerService.getById(event.getSender().getUniqueId());

        if (playerModel == null) {
            return;
        }

        event.setTagValue("rank", ranksConfig.data().getById(playerModel.getRank()).getDisplay());
        event.setTagValue(
            "prestigio",
            prestigeConfig.data().getPrestiges().getById(playerModel.getPrestige()).getDisplay()
        );
    }
}
