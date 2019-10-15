package com.hivemq.backup.mosquitto.format;

import org.jetbrains.annotations.NotNull;

/**
 * @author Florian Limpöck
 * @since 4.2.0
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
