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
