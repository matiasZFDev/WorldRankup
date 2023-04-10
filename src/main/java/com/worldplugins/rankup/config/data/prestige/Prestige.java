package com.worldplugins.rankup.config.data.prestige;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class Prestige {
    private final short id;
    private final @NonNull String display;
    private final @NonNull String group;
    private final Short next;
}
