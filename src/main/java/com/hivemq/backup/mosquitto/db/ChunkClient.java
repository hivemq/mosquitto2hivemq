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
 */
public class ChunkClient {

    private final @NotNull String clientId;
    private final short lastMid;
    private final long sessionExpiryInterval;
    private final long sessionExpiryTime;


    ChunkClient(@NotNull final String clientId, final short lastMid, final long sessionExpiryTime, final int sessionExpiryInterval) {
        this.clientId = clientId;
        this.sessionExpiryTime = sessionExpiryTime;
        this.sessionExpiryInterval = (sessionExpiryInterval != 0xFFFF_FFFF) ? sessionExpiryInterval : 0xFFFF_FFFFL;
        this.lastMid = lastMid;
    }

    public @NotNull String getClientId() {
        return clientId;
    }

    public long getSessionExpiryInterval() {
        return sessionExpiryInterval;
    }

    public boolean isConnected() {
        return false;
    }

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