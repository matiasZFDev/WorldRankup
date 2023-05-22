package com.worldplugins.rankup.listener;

import com.worldplugins.rankup.config.data.earn.*;
import com.worldplugins.rankup.listener.earn.EarnExecutor;
import com.worldplugins.rankup.listener.earn.EarnHandlerType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class ShardEarnListener implements Listener {
    private final @NotNull EarnExecutor earnExecutor;

    public ShardEarnListener(@NotNull EarnExecutor earnExecutor) {
        this.earnExecutor = earnExecutor;
    }

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
        if (!(event.getDamager() instanceof Player)) {
            return;
        }

        tryAllEarns(
            event.getDamager().getWorld().getName(),
            (Player) event.getDamager(),
            MobHitEarn.class,
            earn -> ((MobHitEarn) earn).mobs().contains(event.getEntity().getType())
        );
    }

    @EventHandler
    public void onMobHit(EntityDeathEvent event) {
        if (event.getEntity().getKiller() == null) {
            return;
        }

        tryAllEarns(
            event.getEntity().getWorld().getName(),
            event.getEntity().getKiller(),
            MobKillEarn.class,
            earn -> ((MobKillEarn) earn).mobs().contains(event.getEntity().getType())
        );
    }

    @EventHandler
    public void onFish(PlayerFishEvent event) {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) {
            return;
        }

        tryAllEarns(
            event.getPlayer().getWorld().getName(),
            event.getPlayer(),
            FishEarn.class,
            earn -> true
        );
    }

    private void tryAllEarns(
        @NotNull String worldName,
        @NotNull Player player,
        @NotNull Class<? extends ShardEarn> earnType,
        @NotNull Function<ShardEarn, Boolean> laterChecks
    ) {
        System.out.println("pre");
        earnExecutor.tryEarns(worldName, player, EarnHandlerType.SHARD, earnType, laterChecks);
        System.out.println("mid");
        earnExecutor.tryEarns(worldName, player, EarnHandlerType.LIMIT, earnType, laterChecks);
        System.out.println("done");
    }
}
