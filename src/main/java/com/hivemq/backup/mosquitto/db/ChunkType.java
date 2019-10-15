package com.hivemq.backup.mosquitto.db;

import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.Nullable;

/**
 * @author Lukas Brand
 */
public enum ChunkType {

    DB_CHUNK_CFG(1),
    DB_CHUNK_MSG_STORE(2),
    DB_CHUNK_CLIENT_MSG(3),
    DB_CHUNK_RETAIN(4),
    DB_CHUNK_SUB(5),
    DB_CHUNK_CLIENT(6);

    private final static ImmutableMap<Integer, ChunkType> map;

    static {
        final ImmutableMap.Builder<Integer, ChunkType> builder = ImmutableMap.builder();
        builder.put(1, DB_CHUNK_CFG)
                .put(2, DB_CHUNK_MSG_STORE)
                .put(3, DB_CHUNK_CLIENT_MSG)
                .put(4, DB_CHUNK_RETAIN)
                .put(5, DB_CHUNK_SUB)
                .put(6, DB_CHUNK_CLIENT);
        map = builder.build();
    }

    ChunkType(final int order) {
    }

    public static @Nullable ChunkType getType(final int i) {
        return map.get(i);
    }
}
