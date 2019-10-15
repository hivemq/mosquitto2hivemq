package com.hivemq.backup.mosquitto.db;

import org.jetbrains.annotations.NotNull;

/**
 * @author Lukas Brand
 */
public class ChunkCfg {

    private final byte shutdown;
    private final byte dbIdSize;
    private final long lastDbId;


    ChunkCfg(final byte shutdown, final byte dbIdSize, final long lastDbId) {
        this.shutdown = shutdown;
        this.dbIdSize = dbIdSize;
        this.lastDbId = lastDbId;
    }

    @Override
    public @NotNull String toString() {
        return "ChunkCfg{" +
                "shutdown=" + shutdown +
                ", dbIdSize=" + dbIdSize +
                ", lastDbId=" + lastDbId +
                '}';
    }
}

