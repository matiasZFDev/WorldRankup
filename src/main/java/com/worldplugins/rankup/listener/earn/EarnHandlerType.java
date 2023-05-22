package com.worldplugins.rankup.listener.earn;

import com.worldplugins.rankup.listener.earn.handler.EarnHandler;
import com.worldplugins.rankup.listener.earn.handler.LimitHandler;
import com.worldplugins.rankup.listener.earn.handler.ShardHandler;
import org.jetbrains.annotations.NotNull;

public enum EarnHandlerType {
    SHARD {
        private final @NotNull EarnHandler handler = new ShardHandler();

        @Override
        public @NotNull EarnHandler getHandler() {
            return handler;
        }
    },
    LIMIT {
        private final @NotNull EarnHandler handler = new LimitHandler();

        @Override
        public @NotNull EarnHandler getHandler() {
            return handler;
        }
    };

    public abstract @NotNull EarnHandler getHandler();
}
