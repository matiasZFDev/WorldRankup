package com.worldplugins.rankup.factory;

import com.worldplugins.lib.config.cache.ConfigCache;
import com.worldplugins.lib.extension.GenericExtensions;
import com.worldplugins.lib.extension.NumberFormatExtensions;
import com.worldplugins.lib.extension.bukkit.ItemExtensions;
import com.worldplugins.lib.extension.bukkit.NBTExtensions;
import com.worldplugins.rankup.NBTKeys;
import com.worldplugins.rankup.config.data.MainData;
import com.worldplugins.rankup.config.data.ShardsData;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import net.minecraft.server.v1_8_R3.NBTTagByte;
import net.minecraft.server.v1_8_R3.NBTTagInt;
import org.bukkit.inventory.ItemStack;

@ExtensionMethod({
    ItemExtensions.class,
    NBTExtensions.class,
    GenericExtensions.class,
    NumberFormatExtensions.class
})

@RequiredArgsConstructor
public class ShardFactoryImpl implements ShardFactory {
    private final @NonNull ConfigCache<MainData> mainConfig;
    private final @NonNull ConfigCache<ShardsData> shardsConfig;

    @Override
    public @NonNull ItemStack createShard(byte shardId, int amount) {
        final ShardsData.Shard configShard = shardsConfig.data().getById(shardId);
        return configShard.getItem()
            .display(mainConfig.data().getShardDisplay())
            .nameFormat(
                "@nome".to(configShard.getDisplay()),
                "@quantia".to(Integer.valueOf(amount).suffixed())
            )
            .colorMeta()
            .addReferenceValue(NBTKeys.PHYISIC_SHARD_ID, new NBTTagByte(shardId))
            .addReferenceValue(NBTKeys.PHYISIC_SHARD_AMOUNT, new NBTTagInt(amount));
    }

    @Override
    public @NonNull ItemStack createLimit(byte shardId, int amount) {
        final ShardsData.Shard configShard = shardsConfig.data().getById(shardId);
        return configShard.getLimitItem()
            .display(mainConfig.data().getLimitDisplay())
            .nameFormat(
                "@nome".to(configShard.getDisplay()),
                "@quantia".to(Integer.valueOf(amount).suffixed())
            )
            .colorMeta()
            .addReferenceValue(NBTKeys.PHYISIC_LIMIT_ID, new NBTTagByte(shardId))
            .addReferenceValue(NBTKeys.PHYISIC_LIMIT_AMOUNT, new NBTTagInt(amount));
    }
}
