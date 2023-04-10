package com.worldplugins.rankup.database.cache;

public interface CacheUnloadTimer<K> {
    void prepareUnload(K key);
    void cancel(K key);
    void unloadAll();
}
