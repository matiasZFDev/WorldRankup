package com.worldplugins.rankup.config.data.prestige;

public interface Prestiges {
    Prestige getById(short id);

    Prestige getPrevious(short prestige);
}
