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
 * @since 1.0.0
 */
public class SubscriptionXML {

    public static final @NotNull String ROOT_ELEMENT = "subscriptions";

    public static final @NotNull String SUBSCRIPTION_ELEMENT = "subscription";

    public static final @NotNull String SHARED_SUBSCRIPTION_ELEMENT = "shared-subscription";

    public static final @NotNull String SHARED_SUBSCRIPTION_ID = "shared-subscription-id";

    public static final @NotNull String SHARED_SUBSCRIPTION_ID_BASE_64 = "shared-subscription-id-base64";

    public static final @NotNull String RETAIN_HANDLING = "retain-handling";

    public static final @NotNull String NO_LOCAL = "no-local";

    public static final @NotNull String RETAIN_AS_PUBLISHED = "retain-as-published";

}
