package com.worldplugins.rankup.factory;

import com.worldplugins.lib.util.ItemBuilding;
import com.worldplugins.rankup.NBTKeys;
import com.worldplugins.rankup.config.data.MainData;
import com.worldplugins.rankup.config.data.ShardsData;
import me.post.lib.config.model.ConfigModel;
import me.post.lib.util.Items;
import me.post.lib.util.NBTs;
import me.post.lib.util.NumberFormats;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import static me.post.lib.util.Pairs.to;

public class ShardFactoryImpl implements ShardFactory {
    private final @NotNull ConfigModel<MainData> mainConfig;
    private final @NotNull ConfigModel<ShardsData> shardsConfig;

    public ShardFactoryImpl(@NotNull ConfigModel<MainData> mainConfig, @NotNull ConfigModel<ShardsData> shardsConfig) {
        this.mainConfig = mainConfig;
        this.shardsConfig = shardsConfig;
    }

    @Override
    public @NotNull ItemStack createShard(byte shardId, int amount) {
        final ShardsData.Shard configShard = shardsConfig.data().getById(shardId);
        final ItemStack shardItem = configShard.item().clone();
        ItemBuilding.display(shardItem, mainConfig.data().shardDisplay());
        ItemBuilding.nameFormat(
            shardItem,
            to("@nome", configShard.display()),
            to("@quantia", NumberFormats.suffixed(amount))
        );
        Items.colorMeta(shardItem);
        return NBTs.modifyTags(shardItem, nbtItem -> {
            nbtItem.setByte(NBTKeys.PHYISIC_SHARD_ID, shardId);
            nbtItem.setInteger(NBTKeys.PHYISIC_SHARD_AMOUNT, amount);
        });
    }

    @Override
    public @NotNull ItemStack createLimit(byte shardId, int amount) {
        final ShardsData.Shard configShard = shardsConfig.data().getById(shardId);
        final ItemStack limitItem = configShard.limitItem().clone();
        ItemBuilding.display(limitItem, mainConfig.data().limitDisplay());
        ItemBuilding.nameFormat(
            limitItem,
            to("@nome", configShard.display()),
            to("@quantia", NumberFormats.suffixed(amount))
        );
        Items.colorMeta(limitItem);
        return NBTs.modifyTags(limitItem, nbtItem -> {
            nbtItem.setByte(NBTKeys.PHYISIC_LIMIT_ID, shardId);
            nbtItem.setInteger(NBTKeys.PHYISIC_LIMIT_AMOUNT, amount);
        });
    }
}
