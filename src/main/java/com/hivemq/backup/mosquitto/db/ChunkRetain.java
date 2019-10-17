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

import org.jetbrains.annotations.NotNull;

/**
 * @author Lukas Brand
 * @since 1.0.0
 */
public class ChunkRetain {

    /**
     * The retained messages store id.
     */
    private final long storeId;

    /**
     * Creates a Retain Chunk. This is created for every retained message.
     *
     * @param storeId The retained messages store id.
     */
    ChunkRetain(long storeId) {
        this.storeId = storeId;
    }

    /**
     * To String method for ChunkRetain to print out all fields.
     *
     * @return A String which is formatted to be print out as command line information.
     */
    @Override
    public @NotNull String toString() {
        return "ChunkRetain{" +
                "storeId=" + storeId +
                '}';
    }
}
