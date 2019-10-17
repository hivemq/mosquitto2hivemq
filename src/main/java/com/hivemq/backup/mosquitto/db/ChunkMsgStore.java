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

import com.hivemq.backup.mosquitto.format.PayloadFormatIndicator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


/**
 * @author Lukas Brand
 * @since 1.0
 */
public class ChunkMsgStore {

    /**
     * The messages store id.
     */
    private final long storeId;

    /**
     * The messages source port.
     */
    private final int sourcePort;

    /**
     * The messages source message id.
     */
    private final int sourceMid;

    /**
     * The messages topic which cannot be null.
     */
    private final @NotNull String topic;

    /**
     * The messages quality of service.
     */
    private final byte qos;

    /**
     * The messages retain boolean.
     */
    private final boolean retain;

    /**
     * The messages owning username or client id which cannot be null.
     */
    private final @NotNull String usernameOrId;

    /**
     * The messages payload length.
     */
    private final long payloadLength;

    /**
     * The messages expiry time in seconds.
     */
    private final long expiryTime;

    /**
     * The messages payload which cannot be null.
     */
    private final @NotNull byte[] payload;

    /**
     * The messages properties which cannot be null.
     */
    private final @NotNull List<Property> properties;

    /**
     * Creates a Message Store Chunk. This is created for every saved message.
     *
     * @param storeId       The messages store id.
     * @param sourcePort    The messages source port.
     * @param sourceMid     The messages id.
     * @param topic         The messages topic.
     * @param qos           The messages quality of service.
     * @param retain        The messages retain boolean.
     * @param usernameOrId  The messages owning username or client id.
     * @param payloadLength The messages payload length.
     * @param expiryTime    The messages expiry time in seconds.
     * @param payload       The messages payload.
     * @param properties    The messages properties.
     */
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
        this.retain = retain == 1;
        this.usernameOrId = usernameOrId;
        this.payloadLength = payloadLength;
        this.expiryTime = expiryTime;
        this.payload = payload.getBytes();
        this.properties = properties;
    }

    /**
     * Getter method for store id.
     *
     * @return A positive long.
     */
    public long getStoreId() {
        return storeId;
    }

    /**
     * Getter method for the messages topic.
     *
     * @return A String which cannot be null.
     */
    public @NotNull String getTopic() {
        return topic;
    }

    /**
     * Getter method for the messages quality of service.
     *
     * @return Either 0, 1 or 2.
     */
    public byte getQos() {
        return qos;
    }

    /**
     * Getter method for the messages expiry time in seconds.
     *
     * @return A positive long.
     */
    public long getExpiryTime() {
        return expiryTime;
    }

    /**
     * Getter method for the messages payload.
     *
     * @return A byte array which cannot be null.
     */
    public @NotNull byte[] getPayload() {
        return payload;
    }

    /**
     * Getter method for the messages retain boolean.
     *
     * @return True if message is a retained message otherwise false.
     */
    public boolean getRetain() {
        return retain;
    }

    /**
     * Getter method for the messages content type.
     *
     * @return A String which can be null.
     */
    public @Nullable String getContentType() {
        if (properties.isEmpty()) return null;
        final @Nullable Property contentTypeOrNull = properties.stream()
                .filter(property -> property.getIdentifier()
                        .equals(PropertyType.MQTT_PROP_CONTENT_TYPE))
                .findFirst()
                .orElse(null);
        return (contentTypeOrNull != null) ? contentTypeOrNull.getKey() : null;
    }

    /**
     * Getter method for the messages response topic.
     *
     * @return A String which can be null.
     */
    public @Nullable String getResponseTopic() {
        if (properties.isEmpty()) return null;
        final @Nullable Property responseTopicOrNull = properties.stream()
                .filter(property -> property.getIdentifier()
                        .equals(PropertyType.MQTT_PROP_RESPONSE_TOPIC))
                .findFirst()
                .orElse(null);
        return (responseTopicOrNull != null) ? responseTopicOrNull.getKey() : null;
    }

    /**
     * Getter method for the messages correlation data.
     *
     * @return A String which can be null.
     */
    public @Nullable byte[] getCorrelationData() {
        if (properties.isEmpty()) return null;
        final @Nullable Property correlationDataOrNull = properties.stream()
                .filter(property -> property.getIdentifier()
                        .equals(PropertyType.MQTT_PROP_CORRELATION_DATA))
                .findFirst()
                .orElse(null);
        return (correlationDataOrNull != null) ? correlationDataOrNull.getByteArrayValue() : null;
    }

    /**
     * Getter method for the messages payload format indicator.
     *
     * @return A PayloadFormatIndicator object which can be null.
     */
    public @Nullable PayloadFormatIndicator getPayloadFormatIndicator() {
        if (properties.isEmpty()) return null;
        final @Nullable Property payloadFormatIndicatorOrNull = properties.stream()
                .filter(property -> property.getIdentifier()
                        .equals(PropertyType.MQTT_PROP_PAYLOAD_FORMAT_INDICATOR))
                .findFirst()
                .orElse(null);
        return (payloadFormatIndicatorOrNull != null) ? PayloadFormatIndicator.fromCode(payloadFormatIndicatorOrNull.getByteValue()) : null;
    }

    /**
     * Getter method for the messages user properties.
     *
     * @return A List of user properties which cannot be null.
     */
    public @NotNull List<Property> getUserProperties() {
        return properties.stream()
                .filter(property -> property.getIdentifier()
                        .equals(PropertyType.MQTT_PROP_USER_PROPERTY))
                .collect(Collectors.toList());
    }

    /**
     * To String method for ChunkMsgStore to print out all fields.
     *
     * @return A String which is formatted to be print out as command line information.
     */
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
