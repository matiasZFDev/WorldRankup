package com.worldplugins.rankup.database.dao;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public final class PlayerData {
    private final short rank;
    private final short prestige;
}
