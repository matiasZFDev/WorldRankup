package com.worldplugins.rankup.database.cache;

import lombok.NonNull;

import java.util.Collection;

public interface Cache<K, V> {
    void set(K key, V value);
    V get(K key);
    boolean containsKey(K key);
    @NonNull Collection<V> getValues();
}
