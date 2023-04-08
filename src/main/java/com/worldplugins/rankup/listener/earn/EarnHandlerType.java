package com.worldplugins.rankup.listener.earn;

import com.worldplugins.rankup.listener.earn.handler.EarnHandler;
import com.worldplugins.rankup.listener.earn.handler.LimitHandler;
import com.worldplugins.rankup.listener.earn.handler.ShardHandler;
import lombok.NonNull;

public enum EarnHandlerType {
    SHARD {
        private final @NonNull EarnHandler handler = new ShardHandler();

        @Override
        public @NonNull EarnHandler getHandler() {
            return handler;
        }
    },
    LIMIT {
        private final @NonNull EarnHandler handler = new LimitHandler();

        @Override
        public @NonNull EarnHandler getHandler() {
            return handler;
        }
    };

    public abstract @NonNull EarnHandler getHandler();
}
