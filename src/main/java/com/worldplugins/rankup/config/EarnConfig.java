package com.worldplugins.rankup.config;

import com.worldplugins.lib.common.Logger;
import com.worldplugins.lib.config.bukkit.ConfigContainer;
import com.worldplugins.lib.config.cache.StateConfig;
import com.worldplugins.lib.config.cache.annotation.Config;
import com.worldplugins.lib.extension.GenericExtensions;
import com.worldplugins.lib.extension.bukkit.ConfigurationExtensions;
import com.worldplugins.rankup.config.data.ChanceShard;
import com.worldplugins.rankup.config.data.ShardSendType;
import com.worldplugins.rankup.config.data.earn.*;
import lombok.NonNull;
import lombok.experimental.ExtensionMethod;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;

import java.util.*;
import java.util.stream.Collectors;

@ExtensionMethod({
    GenericExtensions.class,
    ConfigurationExtensions.class
})

@Config(path = "recompensas")
public class EarnConfig extends StateConfig<EarnConfig.Config> {

    public EarnConfig(Logger logger, @NonNull ConfigContainer configContainer) {
        super(logger, configContainer);
    }

    public static class Config {
        private final @NonNull Map<Class<? extends ShardEarn>, List<ShardEarn>> shardEarns;
        private final @NonNull Map<Class<? extends ShardEarn>, List<ShardEarn>> limitEarns;

        public Config(
            @NonNull Collection<ShardEarn> shardEarns,
            @NonNull Collection<ShardEarn> limitEarns
        ) {
            this.shardEarns = shardEarns.stream().collect(Collectors.groupingBy(ShardEarn::getClass));
            this.limitEarns = limitEarns.stream().collect(Collectors.groupingBy(ShardEarn::getClass));
        }

        public List<ShardEarn> getShardEarnsByType(@NonNull Class<? extends ShardEarn> earnType) {
            return shardEarns.get(earnType);
        }

        public List<ShardEarn> getLimitEarnsByType(@NonNull Class<? extends ShardEarn> earnType) {
            return limitEarns.get(earnType);
        }
    }

    @Override
    public @NonNull Config fetch(@NonNull FileConfiguration config) {
        return new Config(
            config.section("Fragmentos").map(this::fetchEarn),
            config.section("Limite").map(this::fetchEarn)
        );
    }

    private @NonNull ShardEarn fetchEarn(@NonNull ConfigurationSection section) {
        final String earnType = section.getString("Tipo");
        final Collection<String> ranks = new HashSet<>(section.getStringList("Ranks"));
        final Collection<String> worlds = new HashSet<>(section.getStringList("Mundos"));
        final Collection<ChanceShard> shards = section.getStringList("Fragmentos").stream()
            .map(shardData -> {
                final String[] shardSplit = shardData.split(":");
                final String name = shardSplit[0];
                final double chance = Double.parseDouble(shardSplit[1]);
                final int amount = shardSplit.length == 2 ? 1 : Integer.parseInt(shardSplit[2]);
                return new ChanceShard(name, chance, amount);
            })
            .collect(Collectors.toList());
        final ShardSendType sendType = ShardSendType.getByName(section.getString("Entrega"));

        if (sendType == null) {
            throw new Error(
                "O tipo de entrega '" + section.getString("Entrega") + " não é valido. Use 'fisico' ou 'virtual'"
            );
        }

        switch (earnType) {
            case "QUEBRAR_BLOCO":
                return new BlockBreakEarn(
                    ranks,
                    worlds,
                    section.getStringList("Blocos").stream()
                        .map(blockData -> {
                            final String[] blockSplit = blockData.split(":");
                            return new BlockBreakEarn.Block(
                                Integer.parseInt(blockSplit[0]), Byte.parseByte(blockSplit[1])
                            );
                        })
                        .collect(Collectors.toList()),
                    shards,
                    sendType
                );

            case "HITAR_MOB":
                return new MobHitEarn(
                    ranks,
                    worlds,
                    section.getStringList("Mobs").stream()
                        .map(EntityType::valueOf)
                        .collect(Collectors.toList()),
                    shards,
                    sendType
                );

            case "MATAR_MOB":
                return new MobKillEarn(
                    ranks,
                    worlds,
                    section.getStringList("Mobs").stream()
                        .map(EntityType::valueOf)
                        .collect(Collectors.toList()),
                    shards,
                    sendType
                );

            case "PESCA":
                return new FishEarn(ranks, worlds, shards, sendType);

            default:
                throw new Error("O tipo de ganho '" + earnType + "' não é válido. Leja a configuração para mais informação.");
        }
    }
}