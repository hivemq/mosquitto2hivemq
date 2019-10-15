package com.hivemq.backup.mosquitto.format;

import org.jetbrains.annotations.NotNull;

/**
 * @author Florian Limpöck
 * @since 4.2.0
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
