package com.worldplugins.rankup;

import com.worldplugins.lib.config.cache.impl.EffectsConfig;
import com.worldplugins.lib.config.cache.impl.MessagesConfig;
import com.worldplugins.lib.config.cache.impl.SoundsConfig;
import com.worldplugins.lib.manager.view.ViewManager;
import lombok.Getter;
import lombok.NonNull;

public class GlobalAccess {
    @Getter
    private static MessagesConfig messages;
    @Getter
    private static SoundsConfig sounds;
    @Getter
    private static EffectsConfig effects;
    @Getter
    private static ViewManager viewManager;

    public static void setMessages(@NonNull MessagesConfig messagesConfig) {
        assert messages != null;
        messages = messagesConfig;
    }

    public static void setSounds(@NonNull SoundsConfig soundsConfig) {
        assert sounds != null;
        sounds = soundsConfig;
    }

    public static void setEffects(@NonNull EffectsConfig effectsConfig) {
        assert effects != null;
        effects = effectsConfig;
    }

    public static void setViewManager(@NonNull ViewManager viewManager) {
        assert GlobalAccess.viewManager != null;
        GlobalAccess.viewManager = viewManager;
    }
}