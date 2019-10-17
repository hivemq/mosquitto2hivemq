/*
 * Copyright 2019 HiveMQ and the HiveMQ Community
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.hivemq.backup.mosquitto.db;

import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.Nullable;

/**
 * @author Lukas Brand
 * @since 1.0
 */
public enum ChunkType {

    DB_CHUNK_CFG(1),
    DB_CHUNK_MSG_STORE(2),
    DB_CHUNK_CLIENT_MSG(3),
    DB_CHUNK_RETAIN(4),
    DB_CHUNK_SUB(5),
    DB_CHUNK_CLIENT(6);

    /**
     * Connects Chunk Type and its Integer value.
     */
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

    /**
     * Creates an enum of Type ChunkType.
     *
     * @param order Used to mark the ChunkType ordinal.
     */
    ChunkType(@SuppressWarnings("unused") final int order) {
    }

    /**
     * Getter method for the Chunk Type based on its integer value.
     *
     * @param i positive integer in range of 1-6.
     * @return Chunk Type based on integer.
     */
    public static @Nullable ChunkType getType(final int i) {
        return map.get(i);
    }
}
