package com.worldplugins.rankup.config.data;

import com.worldplugins.rankup.config.data.prestige.Prestiges;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class PrestigeData {
    private final short defaulPrestige;
    private final @NonNull Prestiges prestiges;
}
