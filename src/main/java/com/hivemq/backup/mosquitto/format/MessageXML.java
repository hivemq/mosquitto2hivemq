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
package com.hivemq.backup.mosquitto.format;

import org.jetbrains.annotations.NotNull;

/**
 * @author Florian Limpöck
 * @author Lukas Brand
 * @since 1.0.0
 */
public class MessageXML {

    public static final @NotNull String PACKET_ID = "packet-id";

    public static final @NotNull String CLUSTER_ID = "cluster-id";

    public static final @NotNull String PUBLISH_ID = "publish-id";

    public static final @NotNull String TOPIC = "topic";

    public static final @NotNull String TOPIC_BASE_64 = "topic-base64";

    public static final @NotNull String TIMESTAMP = "timestamp";

    public static final @NotNull String DUPLICATE_DELIVERY = "duplicate-delivery";

    public static final @NotNull String RETAINED = "retained";

    public static final @NotNull String MESSAGE = "message";

    public static final @NotNull String QOS = "qos";

    public static final @NotNull String MESSAGE_EXPIRY = "message-expiry-interval";

    public static final @NotNull String CORRELATION_DATA = "correlation-data";

    public static final @NotNull String RESPONSE_TOPIC = "response-topic";

    public static final @NotNull String CONTENT_TYPE = "content-type";

    public static final @NotNull String PAYLOAD_FORMAT_INDICATOR = "payload-format-indicator";

    public static final @NotNull String USER_PROPERTIES = "user-properties";

    public static final @NotNull String USER_PROPERTY = "user-property";

    public static final @NotNull String USER_PROPERTY_NAME = "name";

    public static final @NotNull String USER_PROPERTY_VALUE = "value";

    public static final @NotNull String SUBSCRIPTION_IDENTIFIERS = "subscription-identifiers";

    public static final @NotNull String SUBSCRIPTION_IDENTIFIER = "subscription-identifier";

}
