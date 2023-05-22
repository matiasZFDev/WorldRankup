package com.worldplugins.rankup.config;

import com.worldplugins.lib.util.ConfigSections;
import com.worldplugins.rankup.config.data.EarnData;
import com.worldplugins.rankup.config.data.shard.ChanceShard;
import com.worldplugins.rankup.config.data.shard.ShardSendType;
import com.worldplugins.rankup.config.data.earn.*;
import me.post.lib.config.model.ConfigModel;
import me.post.lib.config.wrapper.ConfigWrapper;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.util.*;
import java.util.stream.Collectors;

public class EarnConfig implements ConfigModel<EarnData> {
    private @UnknownNullability EarnData data;
    private final @NotNull ConfigWrapper configWrapper;

    public EarnConfig(@NotNull ConfigWrapper configWrapper) {
        this.configWrapper = configWrapper;
    }

    public void update() {
        final FileConfiguration config = configWrapper.unwrap();
        data = new EarnData(
            ConfigSections.map(config.getConfigurationSection("Fragmentos"), this::fetchEarn),
            ConfigSections.map(config.getConfigurationSection("Limite"), this::fetchEarn)
        );
    }

    private @NotNull ShardEarn fetchEarn(@NotNull ConfigurationSection section) {
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

    @Override
    public @NotNull EarnData data() {
        return data;
    }

    @Override
    public @NotNull ConfigWrapper wrapper() {
        return configWrapper;
    }
}