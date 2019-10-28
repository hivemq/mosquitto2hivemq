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
public class ChunkClient {

    /**
     * The client's id value which cannot be null.
     */
    private final @NotNull String clientId;

    /**
     * The client's last message id which it had an action with.
     */
    private final short lastMid;

    /**
     * The client's session expiry interval in seconds.
     */
    private final long sessionExpiryInterval;

    /**
     * The client's session expiry time in seconds.
     */
    private final long sessionExpiryTime;

    /**
     * Explicit connection status for all clients. Due to the migration every client is not connected to the new Broker.
     */
    @SuppressWarnings("FieldCanBeLocal")
    private final boolean connectionStatus = false;

    /**
     * Creates a Client Chunk. This represents a Client who needs to have an existing Session which has not expired yet.
     *
     * @param clientId              The client's id value which cannot be null.
     * @param lastMid               The client's last message id which it had an action with.
     * @param sessionExpiryTime     The client's session expiry time in seconds.
     * @param sessionExpiryInterval The client's session expiry interval in seconds.
     */
    ChunkClient(final @NotNull String clientId, final short lastMid, final long sessionExpiryTime, final int sessionExpiryInterval) {
        this.clientId = clientId;
        this.sessionExpiryTime = sessionExpiryTime;
        this.sessionExpiryInterval = (sessionExpiryInterval != 0xFFFF_FFFF) ? sessionExpiryInterval : 0xFFFF_FFFFL;
        this.lastMid = lastMid;
    }

    /**
     * Getter method for the client's id.
     *
     * @return A String which is not null.
     */
    public @NotNull String getClientId() {
        return clientId;
    }

    /**
     * Getter method for client's session expiry interval.
     *
     * @return A long which must be positive.
     */
    public long getSessionExpiryInterval() {
        return sessionExpiryInterval;
    }

    /**
     * Explicit getter method for client's connection status.
     *
     * @return Always false.
     */
    public boolean isConnected() {
        return connectionStatus;
    }

    /**
     * To String method for ChunkClient to print out all fields.
     *
     * @return A String which is formatted to be print out as command line information.
     */
    @Override
    public @NotNull String toString() {
        return "ChunkClient{" +
                "clientId='" + clientId + '\'' +
                ", lastMid=" + lastMid +
                ", sessionExpiryInterval=" + sessionExpiryInterval +
                ", sessionExpiryTime=" + sessionExpiryTime +
                '}';
    }
}