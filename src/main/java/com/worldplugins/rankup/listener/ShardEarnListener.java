package com.worldplugins.rankup.listener;

import com.worldplugins.lib.extension.GenericExtensions;
import com.worldplugins.lib.extension.NumberFormatExtensions;
import com.worldplugins.lib.extension.bukkit.PlayerExtensions;
import com.worldplugins.rankup.config.data.earn.*;
import com.worldplugins.rankup.extension.ResponseExtensions;
import com.worldplugins.rankup.listener.earn.EarnExecutor;
import com.worldplugins.rankup.listener.earn.EarnHandlerType;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerFishEvent;

import java.util.function.Function;

@ExtensionMethod({
    GenericExtensions.class,
    ResponseExtensions.class,
    PlayerExtensions.class,
    NumberFormatExtensions.class
})

@RequiredArgsConstructor
public class ShardEarnListener implements Listener {
    private final @NonNull EarnExecutor earnExecutor;

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        tryAllEarns(
            event.getBlock().getWorld().getName(),
            event.getPlayer(),
            BlockBreakEarn.class,
            earn -> ((BlockBreakEarn) earn).hasBlock(event.getBlock())
        );
    }

    @EventHandler
    public void onMobHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player))
            return;

        tryAllEarns(
            event.getDamager().getWorld().getName(),
            (Player) event.getDamager(),
            MobHitEarn.class,
            earn -> ((MobHitEarn) earn).getMobs().contains(event.getEntity().getType())
        );
    }

    @EventHandler
    public void onMobHit(EntityDeathEvent event) {
        if (event.getEntity().getKiller() == null)
            return;

        tryAllEarns(
            event.getEntity().getWorld().getName(),
            event.getEntity().getKiller(),
            MobKillEarn.class,
            earn -> ((MobKillEarn) earn).getMobs().contains(event.getEntity().getType())
        );
    }

    @EventHandler
    public void onFish(PlayerFishEvent event) {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH)
            return;

        tryAllEarns(
            event.getPlayer().getWorld().getName(),
            event.getPlayer(),
            FishEarn.class,
            earn -> true
        );
    }

    private void tryAllEarns(
        @NonNull String worldName,
        @NonNull Player player,
        @NonNull Class<? extends ShardEarn> earnType,
        @NonNull Function<ShardEarn, Boolean> laterChecks
    ) {
        earnExecutor.tryEarns(worldName, player, EarnHandlerType.SHARD, earnType, laterChecks);
        earnExecutor.tryEarns(worldName, player, EarnHandlerType.LIMIT, earnType, laterChecks);
    }
}
