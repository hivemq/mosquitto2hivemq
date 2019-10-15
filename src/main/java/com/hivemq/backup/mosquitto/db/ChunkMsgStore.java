package com.hivemq.backup.mosquitto.db;

import com.hivemq.backup.mosquitto.format.PayloadFormatIndicator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 * @author Lukas Brand
 */
public class ChunkMsgStore {
    private final long storeId;
    private final int sourcePort;
    private final int sourceMid;
    private final @NotNull String topic;
    private final byte qos;
    private final byte retain;
    private final @NotNull String usernameOrId;
    private final long payloadLength;
    private final long expiryTime;
    private final @NotNull byte[] payload;
    private final @NotNull List<Property> properties;

    ChunkMsgStore(final long storeId,
                  final short sourcePort,
                  final short sourceMid,
                  final @NotNull String topic,
                  final byte qos,
                  final byte retain,
                  final @NotNull String usernameOrId,
                  final int payloadLength,
                  final long expiryTime,
                  final @NotNull String payload,
                  final @NotNull List<Property> properties) {
        this.storeId = storeId;
        this.sourcePort = sourcePort;
        this.sourceMid = sourceMid;
        this.topic = topic;
        this.qos = qos;
        this.retain = retain;
        this.usernameOrId = usernameOrId;
        this.payloadLength = payloadLength;
        this.expiryTime = expiryTime;
        this.payload = payload.getBytes();
        this.properties = properties;
    }

    public long getStoreId() {
        return storeId;
    }

    public @NotNull String getTopic() {
        return topic;
    }

    public byte getQos() {
        return qos;
    }

    public long getExpiryTime() {
        return expiryTime;
    }

    public @NotNull byte[] getPayload() {
        return payload;
    }

    public byte getRetain() {
        return retain;
    }

    private @NotNull List<Property> getProperties() {
        return properties;
    }

    public @Nullable String getContentType() {
        if (properties.isEmpty()) return null;
        return Objects.requireNonNull(getProperties().stream()
                .filter(property -> property.getIdentifier()
                        .equals(PropertyType.MQTT_PROP_CONTENT_TYPE))
                .findFirst()
                .orElse(null))
                .getKey();
    }

    public @Nullable String getResponseTopic() {
        if (properties.isEmpty()) return null;
        return Objects.requireNonNull(getProperties().stream()
                .filter(property -> property.getIdentifier()
                        .equals(PropertyType.MQTT_PROP_RESPONSE_TOPIC))
                .findFirst()
                .orElse(null))
                .getKey();
    }

    public @Nullable byte[] getCorrelationData() {
        if (properties.isEmpty()) return null;
        return Objects.requireNonNull(getProperties().stream()
                .filter(property -> property.getIdentifier()
                        .equals(PropertyType.MQTT_PROP_CORRELATION_DATA))
                .findFirst().orElse(null))
                .getByteArrayValue();
    }

    public @Nullable PayloadFormatIndicator getPayloadFormatIndicator() {
        if (properties.isEmpty()) return null;
        return PayloadFormatIndicator.fromCode(Objects.requireNonNull(getProperties().stream()
                .filter(property -> property.getIdentifier()
                        .equals(PropertyType.MQTT_PROP_PAYLOAD_FORMAT_INDICATOR))
                .findFirst()
                .orElse(null))
                .getByteValue());
    }

    public @NotNull List<Property> getUserProperties() {
        return getProperties().stream()
                .filter(property -> property.getIdentifier()
                        .equals(PropertyType.MQTT_PROP_USER_PROPERTY))
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "ChunkMsgStore{" +
                "storeId=" + storeId +
                ", sourcePort=" + sourcePort +
                ", sourceMid=" + sourceMid +
                ", topic='" + topic + '\'' +
                ", qos=" + qos +
                ", retain=" + retain +
                ", usernameOrId='" + usernameOrId + '\'' +
                ", payloadLength=" + payloadLength +
                ", expiryTime=" + expiryTime +
                ", payload=" + Arrays.toString(payload) +
                ", properties=" + properties +
                '}';
    }
}
