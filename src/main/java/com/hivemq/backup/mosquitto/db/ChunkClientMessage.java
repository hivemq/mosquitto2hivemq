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

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Lukas Brand
 * @since 1.0.0
 */
public class ChunkClientMessage {

    /**
     * The client messages store id.
     */
    private final long storeId;

    /**
     * The client messages id.
     */
    private final int mid;

    /**
     * The client messages quality of service.
     */
    private final byte qos;

    /**
     * The client messages state (not used in migration).
     */
    private final byte state;

    /**
     * The client messages duplicate boolean.
     */
    private final boolean retainDuplicate;

    /**
     * The client messages direction value (not used in migration).
     */
    private final byte direction;

    /**
     * The client messages owning client id.
     */
    private final @NotNull String clientId;

    /**
     * The client messages properties (mainly subscription identifier).
     */
    private final @NotNull List<Property> properties;

    /**
     * Creates a Client Message Chunk. This requires an existing client session to be queued.
     *
     * @param storeId         The message store id.
     * @param mid             The message id.
     * @param qos             The messages quality of service.
     * @param state           The messages state.
     * @param retainDuplicate The message being a duplicate or not.
     * @param direction       The messages direction.
     * @param clientId        The client id of the messages owning client.
     * @param properties      The messages properties.
     */
    ChunkClientMessage(final long storeId,
                       final int mid,
                       final byte qos,
                       final byte state,
                       final byte retainDuplicate,
                       final byte direction,
                       final @NotNull String clientId,
                       final @NotNull List<Property> properties) {
        this.storeId = storeId;
        this.mid = mid;
        this.qos = qos;
        this.state = state;
        this.retainDuplicate = retainDuplicate == 1;
        this.direction = direction;
        this.clientId = clientId;
        this.properties = properties;
    }

    /**
     * Getter method for the messages store id.
     *
     * @return A positive long.
     */
    public long getStoreId() {
        return storeId;
    }

    /**
     * Getter method for the messages id. Used for sorting the messages in the right order.
     *
     * @return A positive integer.
     */
    public int getMid() {
        return mid;
    }

    /**
     * Getter method for the Quality of Service of the message.
     *
     * @return Either 0, 1 or 2.
     */
    public byte getQos() {
        return qos;
    }

    /**
     * Getter method for a boolean which indicates a message being a duplicate or not.
     *
     * @return True if message is a duplicate otherwise false.
     */
    public boolean getRetainDuplicate() {
        return retainDuplicate;
    }

    /**
     * Getter method for the client id of the messages owning client.
     *
     * @return A String which cannot be null.
     */
    public @NotNull String getClientId() {
        return clientId;
    }

    /**
     * Getter method for the List of Subscription Identifiers.
     *
     * @return A List which cannot be null.
     */
    public @NotNull List<Integer> getSubscriptionIdentifier() {
        return properties.stream()
                .filter(property -> property.getIdentifier() == PropertyType.MQTT_PROP_SUBSCRIPTION_IDENTIFIER)
                .map(Property::getIntValueOrVar)
                .collect(Collectors.toList());
    }

    /**
     * To String method for ChunkClientMessage to print out all fields.
     *
     * @return A String which is formatted to be print out as command line information.
     */
    @Override
    public @NotNull String toString() {
        return "ChunkClientMessage{" +
                "storeId=" + storeId +
                ", mid=" + mid +
                ", qos=" + qos +
                ", state=" + state +
                ", retainDuplicate=" + retainDuplicate +
                ", direction=" + direction +
                ", clientId='" + clientId + '\'' +
                ", properties=" + properties +
                '}';
    }
}
