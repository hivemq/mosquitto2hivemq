package com.hivemq.backup.mosquitto.format;

import org.jetbrains.annotations.NotNull;

/**
 * @author Florian Limpöck
 * @since 4.2.0
 */
public class ClientSessionXML {

    public static final @NotNull String ROOT_ELEMENT = "client-session";

    public static final @NotNull String CLIENT_ID = "client-id";

    public static final @NotNull String CLIENT_ID_BASE_64 = "client-id-base64";

    public static final @NotNull String DISCONNECTED_SINCE = "disconnected-since";

    public static final @NotNull String SESSION_EXPIRY = "session-expiry-interval";

    public static final @NotNull String CONNECTED = "connected";

    public static final @NotNull String ATTRIBUTES = "attributes";

    public static final @NotNull String ATTRIBUTE = "attribute";

    public static final @NotNull String KEY = "key";

    public static final @NotNull String VALUE = "value";

    public static final @NotNull String TIMESTAMP = "timestamp";


}
