package com.worldplugins.rankup.database.dao;

public final class PlayerData {
    private final short rank;
    private final short prestige;

    public PlayerData(short rank, short prestige) {
        this.rank = rank;
        this.prestige = prestige;
    }

    public short rank() {
        return rank;
    }

    public short prestige() {
        return prestige;
    }
}
