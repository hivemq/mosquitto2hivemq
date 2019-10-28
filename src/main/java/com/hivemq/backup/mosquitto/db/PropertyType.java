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

import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.Nullable;

/**
 * @author Lukas Brand
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public enum PropertyType {

    MQTT_PROP_PAYLOAD_FORMAT_INDICATOR(1),        /* Byte :				PUBLISH, Will Properties */
    MQTT_PROP_MESSAGE_EXPIRY_INTERVAL(2),        /* 4 byte int :			PUBLISH, Will Properties */
    MQTT_PROP_CONTENT_TYPE(3),                    /* UTF-8 string :		PUBLISH, Will Properties */
    MQTT_PROP_RESPONSE_TOPIC(8),                /* UTF-8 string :		PUBLISH, Will Properties */
    MQTT_PROP_CORRELATION_DATA(9),                /* Binary Data :		PUBLISH, Will Properties */
    MQTT_PROP_SUBSCRIPTION_IDENTIFIER(11),        /* Variable byte int :	PUBLISH, SUBSCRIBE */
    MQTT_PROP_SESSION_EXPIRY_INTERVAL(17),        /* 4 byte int :			CONNECT, CONNACK, DISCONNECT */
    MQTT_PROP_ASSIGNED_CLIENT_IDENTIFIER(18),    /* UTF-8 string :		CONNACK */
    MQTT_PROP_SERVER_KEEP_ALIVE(19),            /* 2 byte int :			CONNACK */
    MQTT_PROP_AUTHENTICATION_METHOD(21),        /* UTF-8 string :		CONNECT, CONNACK, AUTH */
    MQTT_PROP_AUTHENTICATION_DATA(22),            /* Binary Data :		CONNECT, CONNACK, AUTH */
    MQTT_PROP_REQUEST_PROBLEM_INFORMATION(23),    /* Byte :				CONNECT */
    MQTT_PROP_WILL_DELAY_INTERVAL(24),            /* 4 byte int :			Will properties */
    MQTT_PROP_REQUEST_RESPONSE_INFORMATION(25),/* Byte :				CONNECT */
    MQTT_PROP_RESPONSE_INFORMATION(26),        /* UTF-8 string :		CONNACK */
    MQTT_PROP_SERVER_REFERENCE(28),            /* UTF-8 string :		CONNACK, DISCONNECT */
    MQTT_PROP_REASON_STRING(31),                /* UTF-8 string :		All except Will properties */
    MQTT_PROP_RECEIVE_MAXIMUM(33),                /* 2 byte int :			CONNECT, CONNACK */
    MQTT_PROP_TOPIC_ALIAS_MAXIMUM(34),            /* 2 byte int :			CONNECT, CONNACK */
    MQTT_PROP_TOPIC_ALIAS(35),                    /* 2 byte int :			PUBLISH */
    MQTT_PROP_MAXIMUM_QOS(36),                    /* Byte :				CONNACK */
    MQTT_PROP_RETAIN_AVAILABLE(37),            /* Byte :				CONNACK */
    MQTT_PROP_USER_PROPERTY(38),                /* UTF-8 string pair :	All */
    MQTT_PROP_MAXIMUM_PACKET_SIZE(39),            /* 4 byte int :			CONNECT, CONNACK */
    MQTT_PROP_WILDCARD_SUB_AVAILABLE(40),        /* Byte :				CONNACK */
    MQTT_PROP_SUBSCRIPTION_ID_AVAILABLE(41),    /* Byte :				CONNACK */
    MQTT_PROP_SHARED_SUB_AVAILABLE(42);        /* Byte :				CONNACK */

    /**
     * Connects Property Type and its Integer value.
     */
    private final static ImmutableMap<Integer, PropertyType> propertyTypesMap;

    static {
        final ImmutableMap.Builder<Integer, PropertyType> builder = ImmutableMap.builder();
        builder.put(1, MQTT_PROP_PAYLOAD_FORMAT_INDICATOR)
                .put(2, MQTT_PROP_MESSAGE_EXPIRY_INTERVAL)
                .put(3, MQTT_PROP_CONTENT_TYPE)
                .put(8, MQTT_PROP_RESPONSE_TOPIC)
                .put(9, MQTT_PROP_CORRELATION_DATA)
                .put(11, MQTT_PROP_SUBSCRIPTION_IDENTIFIER)
                .put(38, MQTT_PROP_USER_PROPERTY);
        propertyTypesMap = builder.build();
    }

    /**
     * Creates an enum of Type PropertyType.
     *
     * @param order Used to mark the PropertyType ordinal.
     */
    PropertyType(final int order) {
    }

    /**
     * Getter method for the Property Type based on its integer value.
     *
     * @param i positive integer in range of 1-42.
     * @return Property Type based on integer.
     */
    public static @Nullable PropertyType getType(final int i) {
        return propertyTypesMap.get(i);
    }
}
