package com.worldplugins.rankup.database.cache;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.Map;

@RequiredArgsConstructor
public class SimpleCache<K, V> implements Cache<K, V> {
    private final @NonNull Map<K, V> data;

    @Override
    public void set(K key, V value) {
        data.put(key, value);
    }

    @Override
    public V get(K key) {
        return data.get(key);
    }

    @Override
    public boolean containsKey(K key) {
        return data.containsKey(key);
    }

    @Override
    public @NonNull Collection<V> getValues() {
        return data.values();
    }
}
