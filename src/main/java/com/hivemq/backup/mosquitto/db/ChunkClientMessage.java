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
 */
public class ChunkClientMessage {

    private final long storeId;
    private final int mid;
    private final byte qos;
    private final byte state;
    private final byte retainDuplicate;
    private final byte direction;
    private final @NotNull String clientId;
    private final @NotNull List<Property> properties;

    ChunkClientMessage(long storeId, final int mid, byte qos, byte state, byte retainDuplicate, byte direction, final @NotNull String clientId, final @NotNull List<Property> properties) {
        this.storeId = storeId;
        this.mid = mid;
        this.qos = qos;
        this.state = state;
        this.retainDuplicate = retainDuplicate;
        this.direction = direction;
        this.clientId = clientId;
        this.properties = properties;
    }

    public long getStoreId() {
        return storeId;
    }

    public int getMid() {
        return mid;
    }

    public byte getQos() {
        return qos;
    }

    public byte getRetainDuplicate() {
        return retainDuplicate;
    }

    public @NotNull String getClientId() {
        return clientId;
    }

    public @NotNull List<Integer> getSubscriptionIdentifier() {
        return properties.stream().filter(property -> property.getIdentifier() == PropertyType.MQTT_PROP_SUBSCRIPTION_IDENTIFIER).map(Property::getIntValueOrVar).collect(Collectors.toList());
    }

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
