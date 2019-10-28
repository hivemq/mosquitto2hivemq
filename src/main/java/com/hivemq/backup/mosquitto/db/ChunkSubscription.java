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
public class ChunkSubscription {

    /**
     * The subscription identifier.
     */
    private final int identifier;

    /**
     * The subscription's quality of service.
     */
    private final byte qos;

    /**
     * The subscription's owning client id which cannot be null.
     */
    private final @NotNull String clientId;

    /**
     * The subscription's topic which cannot be null.
     */
    private final @NotNull String topic;

    /**
     * The subscription's no local boolean.
     */
    private final boolean isNoLocal;

    /**
     * The subscription's retain as published boolean.
     */
    private final boolean isRetainAsPublished;

    /**
     * The subscription's retain handling type.
     */
    private final byte retainHandling;

    /**
     * Creates a Subscription Chunk. This needs an existing client session.
     *
     * @param identifier The subscription identifier.
     * @param qos        The subscription quality of service.
     * @param options    The subscription options containing no local, retain as published as well as retain handling.
     * @param clientId   The subscription's owning client id.
     * @param topic      The subscription's topic.
     */
    ChunkSubscription(final int identifier,
                      final byte qos,
                      final byte options,
                      final @NotNull String clientId,
                      final @NotNull String topic) {
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

    /**
     * Getter method for the subscription identifier.
     *
     * @return A positive integer.
     */
    public int getIdentifier() {
        return identifier;
    }

    /**
     * Getter method for the subscription's quality of service.
     *
     * @return Either 0, 1 or 2.
     */
    public byte getQos() {
        return qos;
    }

    /**
     * Getter method for the subscription's owning client id.
     *
     * @return A String which cannot be null.
     */
    public @NotNull String getClientId() {
        return clientId;
    }

    /**
     * Getter method for the subscription topic.
     *
     * @return A String which cannot be null.
     */
    public @NotNull String getTopic() {
        return topic;
    }

    /**
     * Getter method for the subscription's no local boolean.
     *
     * @return True if subscription is no local otherwise false.
     */
    public boolean isNoLocal() {
        return isNoLocal;
    }

    /**
     * Getter method for the subscription's no retain as published boolean.
     *
     * @return True if subscription is retain as published otherwise false.
     */
    public boolean isRetainAsPublished() {
        return isRetainAsPublished;
    }

    /**
     * Getter method for the subscription's retain handling type.
     *
     * @return Either 0, 1 or 2.
     */
    public byte getRetainHandling() {
        return retainHandling;
    }

    /**
     * To String method for ChunkSubscription to print out all fields.
     *
     * @return A String which is formatted to be print out as command line information.
     */
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
