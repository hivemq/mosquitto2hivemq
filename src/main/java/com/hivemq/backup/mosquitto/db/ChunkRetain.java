package com.hivemq.backup.mosquitto.db;

import org.jetbrains.annotations.NotNull;

/**
 * @author Lukas Brand
 */
public class ChunkRetain {

    private final long storeId;

    ChunkRetain(long storeId) {
        this.storeId = storeId;
    }

    @Override
    public @NotNull String toString() {
        return "ChunkRetain{" +
                "storeId=" + storeId +
                '}';
    }
}
