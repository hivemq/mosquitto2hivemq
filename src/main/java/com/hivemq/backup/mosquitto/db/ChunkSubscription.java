package com.hivemq.backup.mosquitto.db;

import org.jetbrains.annotations.NotNull;

/**
 * @author Lukas Brand
 */
public class ChunkSubscription {

    private final int identifier;
    private final byte qos;
    private final @NotNull String clientId;
    private final @NotNull String topic;
    private final boolean isNoLocal;
    private final boolean isRetainAsPublished;
    private final byte retainHandling;

    ChunkSubscription(final int identifier, byte qos, byte options, final @NotNull String clientId, final @NotNull String topic) {
        this.identifier = identifier;
        this.qos = qos;
        this.clientId = clientId;
        this.topic = topic;
        this.isNoLocal = (options & 0x04) == 4;
        this.isRetainAsPublished = (options & 0x08) == 8;

        if ((options & 0x20) == 32) {
            retainHandling = 2;
        } else if ((options & 0x10) == 16) {
            retainHandling = 1;
        } else {
            retainHandling = 0;
        }
    }

    public int getIdentifier() {
        return identifier;
    }

    public byte getQos() {
        return qos;
    }

    public @NotNull String getClientId() {
        return clientId;
    }

    public @NotNull String getTopic() {
        return topic;
    }

    public boolean isNoLocal() {
        return isNoLocal;
    }

    public boolean isRetainAsPublished() {
        return isRetainAsPublished;
    }

    public byte getRetainHandling() {
        return retainHandling;
    }

    @Override
    public @NotNull String toString() {
        return "ChunkSubscription{" +
                "identifier=" + identifier +
                ", qos=" + qos +
                ", clientId='" + clientId + '\'' +
                ", topic='" + topic + '\'' +
                ", isNoLocal=" + isNoLocal +
                ", isRetainAsPublished=" + isRetainAsPublished +
                ", retainHandling=" + retainHandling +
                '}';
    }
}
