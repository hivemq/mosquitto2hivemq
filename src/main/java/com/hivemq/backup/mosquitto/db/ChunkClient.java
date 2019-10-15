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