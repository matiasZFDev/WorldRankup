package com.worldplugins.rankup.config.data.shard;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class ChanceShard {
    private final String name;
    private final double chance;
    private final int amount;
}
