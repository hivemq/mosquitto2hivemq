package com.hivemq.backup.mosquitto.format;

import org.jetbrains.annotations.NotNull;

/**
 * @author Florian Limpöck
 * @since 4.2.0
 */
public class QueuedMessageXML {

    public static final @NotNull String ROOT_ELEMENT = "queued-messages";

    public static final @NotNull String QUEUED_MESSAGE_ELEMENT = "queued-message";

    public static final @NotNull String TYPE = "type";

    public static final @NotNull String FROM_RETAINED_MESSAGE = "from-retained-message";

    public enum QueuedMessageType {
        PUBLISH("PUBLISH"), PUBREL("PUBREL");

        final @NotNull String name;

        QueuedMessageType(final @NotNull String name) {
            this.name = name;
        }

        
        public static @NotNull QueuedMessageType forName(final @NotNull String name) {

            final @NotNull QueuedMessageType[] queuedMessageTypes = values();
            for (final @NotNull QueuedMessageType queuedMessageType : queuedMessageTypes) {
                if (queuedMessageType.getName().equals(name)) {
                    return queuedMessageType;
                }
            }
            throw new IllegalArgumentException("No queued message type found for name: " + name);
        }

        
        public @NotNull String getName() {
            return name;
        }

    }

}
