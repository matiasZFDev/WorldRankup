package com.worldplugins.rankup.config.data.earn;

import com.worldplugins.rankup.config.data.shard.ChanceShard;
import com.worldplugins.rankup.config.data.shard.ShardCompensation;
import com.worldplugins.rankup.config.data.shard.ShardSendType;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

public class BlockBreakEarn extends ShardEarn {
    @RequiredArgsConstructor
    @Getter
    public static class Block {
        private final int id;
        private final byte data;
    }

    private final @NonNull Map<Integer, Set<Byte>> blocks;

    public BlockBreakEarn(
        @NonNull Collection<String> ranks,
        @NonNull Collection<String> worlds,
        @NonNull Collection<Block> blocks,
        @NonNull Collection<ChanceShard> shards,
        @NonNull ShardSendType sendType
    ) {
        super(ranks, worlds, shards, sendType, ShardCompensation.BLOCK_BREAK);
        this.blocks = blocks.stream().collect(
            Collectors.groupingBy(Block::getId, Collectors.mapping(Block::getData, Collectors.toSet()))
        );
    }

    @SuppressWarnings("deprecation")
    public boolean hasBlock(@NonNull org.bukkit.block.Block bukkitBlock) {
        return blocks
            .getOrDefault(bukkitBlock.getTypeId(), Collections.emptySet())
            .contains(bukkitBlock.getData());
    }
}
