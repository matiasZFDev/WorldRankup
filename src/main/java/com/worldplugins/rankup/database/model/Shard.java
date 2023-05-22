package com.worldplugins.rankup.database.model;

public class Shard {
    private final byte id;
    private int amount;
    private int limit;

    public Shard(byte id, int amount, int limit) {
        this.id = id;
        this.amount = amount;
        this.limit = limit;
    }

    public byte id() {
        return id;
    }

    public int amount() {
        return amount;
    }

    public int limit() {
        return limit;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }
}
