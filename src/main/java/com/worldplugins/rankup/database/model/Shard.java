package com.worldplugins.rankup.database.model;

import lombok.*;

@AllArgsConstructor
@Getter
@Setter
public class Shard {
    private final byte id;
    private int amount;
    private int limit;
}
