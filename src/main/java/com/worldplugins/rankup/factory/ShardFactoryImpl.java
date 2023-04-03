package com.worldplugins.rankup.factory;

import com.worldplugins.lib.extension.GenericExtensions;
import com.worldplugins.lib.extension.NumberFormatExtensions;
import com.worldplugins.lib.extension.bukkit.ItemExtensions;
import com.worldplugins.lib.extension.bukkit.NBTExtensions;
import com.worldplugins.rankup.NBTKeys;
import com.worldplugins.rankup.config.MainConfig;
import com.worldplugins.rankup.config.ShardsConfig;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import net.minecraft.server.v1_8_R3.NBTTagByte;
import org.bukkit.inventory.ItemStack;

@ExtensionMethod({
    ItemExtensions.class,
    NBTExtensions.class,
    GenericExtensions.class,
    NumberFormatExtensions.class
})

@RequiredArgsConstructor
public class ShardFactoryImpl implements ShardFactory {
    private final @NonNull MainConfig mainConfig;
    private final @NonNull ShardsConfig shardsConfig;

    @Override
    public @NonNull ItemStack createShard(byte shardId, int amount) {
        final ShardsConfig.Config.Shard configShard = shardsConfig.get().getById(shardId);
        return configShard.getItem()
            .display(mainConfig.get().getShardDisplay())
            .nameFormat(
                "@nome".to(configShard.getDisplay()),
                "@quantia".to(Integer.valueOf(amount).suffixed())
            )
            .colorMeta()
            .addReferenceValue(NBTKeys.PHYISIC_SHARD, new NBTTagByte(shardId));
    }

    @Override
    public @NonNull ItemStack createLimit(byte shardId, int amount) {
        final ShardsConfig.Config.Shard configShard = shardsConfig.get().getById(shardId);
        return configShard.getLimitItem()
            .display(mainConfig.get().getLimitDisplay())
            .nameFormat(
                "@nome".to(configShard.getDisplay()),
                "@quantia".to(Integer.valueOf(amount).suffixed())
            )
            .colorMeta()
            .addReferenceValue(NBTKeys.PHYISIC_SHARD, new NBTTagByte(shardId));
    }
}
