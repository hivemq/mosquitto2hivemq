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
public class ChunkCfg {

    /**
     * Shutdown value of database.
     */
    private final byte shutdown;

    /**
     * Id size of Database.
     */
    private final byte dbIdSize;

    /**
     * Last stored message in the Database.
     */
    private final long lastDbId;

    /**
     * Creates a Configuration Chunk. There is only one per database file.
     * @param shutdown Internal Mosquitto value, not important for migration.
     * @param dbIdSize Internal Mosquitto value, not important for migration.
     * @param lastDbId Shows the highest stored id of the database. Not important for migration.
     */
    ChunkCfg(final byte shutdown, final byte dbIdSize, final long lastDbId) {
        this.shutdown = shutdown;
        this.dbIdSize = dbIdSize;
        this.lastDbId = lastDbId;
    }

    /**
     * To String method for ChunkCfg to print out all fields.
     * @return A String which is formatted to be print out as command line information.
     */
    @Override
    public @NotNull String toString() {
        return "ChunkCfg{" +
                "shutdown=" + shutdown +
                ", dbIdSize=" + dbIdSize +
                ", lastDbId=" + lastDbId +
                '}';
    }
}

