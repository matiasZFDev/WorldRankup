package com.worldplugins.rankup.util;

import lombok.NonNull;

import java.util.function.Function;

public class InlineCurry {
    public static <V, T, R> Function<V, Function<T, R>> curry(@NonNull Function<V, Function<T, R>> producer) {
        return producer;
    }
}
