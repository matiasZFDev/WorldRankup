package com.worldplugins.rankup.config.data.earn;

import com.worldplugins.rankup.config.data.shard.ChanceShard;
import com.worldplugins.rankup.config.data.shard.ShardCompensation;
import com.worldplugins.rankup.config.data.shard.ShardSendType;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class BlockBreakEarn extends ShardEarn {
    public static class Block {
        private final int id;
        private final byte data;

        public Block(int id, byte data) {
            this.id = id;
            this.data = data;
        }

        public int id() {
            return id;
        }

        public byte data() {
            return data;
        }
    }

    private final @NotNull Map<Integer, Set<Byte>> blocks;

    public BlockBreakEarn(
        @NotNull Collection<String> ranks,
        @NotNull Collection<String> worlds,
        @NotNull Collection<Block> blocks,
        @NotNull Collection<ChanceShard> shards,
        @NotNull ShardSendType sendType
    ) {
        super(ranks, worlds, shards, sendType, ShardCompensation.BLOCK_BREAK);
        this.blocks = blocks.stream().collect(
            Collectors.groupingBy(Block::id, Collectors.mapping(Block::data, Collectors.toSet()))
        );
    }

    @SuppressWarnings("deprecation")
    public boolean hasBlock(@NotNull org.bukkit.block.Block bukkitBlock) {
        return blocks
            .getOrDefault(bukkitBlock.getTypeId(), Collections.emptySet())
            .contains(bukkitBlock.getData());
    }
}
