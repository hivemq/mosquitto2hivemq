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
package com.hivemq.backup.mosquitto.xml;

import com.google.common.base.Preconditions;
import com.hivemq.backup.mosquitto.db.ChunkClient;
import com.hivemq.backup.mosquitto.db.ChunkClientMessage;
import com.hivemq.backup.mosquitto.db.ChunkMsgStore;
import com.hivemq.backup.mosquitto.db.ChunkSubscription;
import com.hivemq.backup.mosquitto.format.*;
import com.hivemq.backup.mosquitto.utils.DataExportUtil;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.hivemq.backup.mosquitto.format.GlobalXML.EXPORTED_AT;
import static com.hivemq.backup.mosquitto.utils.DataExportUtil.BASE_64;
import static com.hivemq.backup.mosquitto.utils.DataExportUtil.getNextFileName;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author Florian Limpöck
 * @since 1.0.0
 */
public class ClientSessionExporter {

    private final XMLOutputFactory factory = XMLOutputFactory.newInstance();
    private final String clusterId;
    private final String hiveMqVersion;
    private final Path fileSaveLocation;
    private final long timestamp;
    @SuppressWarnings("FieldCanBeLocal")
    private long SESSION_EXPIRE_ON_DISCONNECT = 0;

    /**
     * Creates a ClientSessionExporter.
     *
     * @param timestamp        Creation timestamp.
     * @param fileSaveLocation The folder the client sessions get saved to.
     * @param clusterId        The HiveMQ cluster id.
     * @param hiveMqVersion    The used HiveMQ version.
     */
    public ClientSessionExporter(final long timestamp,
                                 final @NotNull Path fileSaveLocation,
                                 final @NotNull String clusterId,
                                 final @NotNull String hiveMqVersion) {
        this.timestamp = timestamp;
        this.fileSaveLocation = fileSaveLocation;
        this.clusterId = clusterId;
        this.hiveMqVersion = hiveMqVersion;
    }

    /**
     * Writes all client sessions to XML.
     *
     * @param clients       All client sessions.
     * @param subscriptions All client subscriptions.
     * @param clientMsgs    All client messages.
     * @param msgStore      All stored messages.
     */
    public void writeToXml(final @NotNull List<ChunkClient> clients,
                           final @NotNull List<ChunkSubscription> subscriptions,
                           final @NotNull List<ChunkClientMessage> clientMsgs,
                           final @NotNull List<ChunkMsgStore> msgStore) {

        try {
            for (final @NotNull ChunkClient client : clients) {

                final @NotNull String clientID = client.getClientId();

                Preconditions.checkNotNull(client, "client session must not be null");

                // persistent only
                if (client.getSessionExpiryInterval() == SESSION_EXPIRE_ON_DISCONNECT) {
                    continue;
                }

                final @NotNull File sessionsFolder = new File(fileSaveLocation.toFile(), "client-sessions");

                FileUtils.forceMkdir(sessionsFolder);

                final @NotNull String fileName = getNextFileName(sessionsFolder, clientID);

                try (final @NotNull FileOutputStream fileOutputStream = new FileOutputStream(new @NotNull File(sessionsFolder, clusterId + "-" + fileName + ".xml"))) {
                    final @NotNull XMLStreamWriter xmlStreamWriter = factory.createXMLStreamWriter(fileOutputStream);
                    try {
                        xmlStreamWriter.writeStartDocument(); //  <?xml version="1.0" ?>
                        xmlStreamWriter.writeCharacters("\n");
                        xmlStreamWriter.writeStartElement(ClientSessionXML.ROOT_ELEMENT);

                        writeClientSessionAttributes(xmlStreamWriter, client, timestamp);
                        writeSubscriptionInfo(xmlStreamWriter, clientID, subscriptions);
                        writeQueuedMessagesInfo(xmlStreamWriter, clientID, clientMsgs, msgStore);

                        xmlStreamWriter.writeEndElement();

                    } finally {
                        xmlStreamWriter.close();
                    }
                }

            }

        } catch (final @NotNull IOException | @NotNull XMLStreamException ex) {
            ex.printStackTrace();
        }
    }


    private void writeSubscriptionInfo(final @NotNull XMLStreamWriter xmlStreamWriter,
                                       final @NotNull String clientId,
                                       final @NotNull List<ChunkSubscription> allSubscriptions) throws XMLStreamException {

        final @NotNull List<ChunkSubscription> subscriptions = allSubscriptions.stream()
                .filter(chunkSubscription -> chunkSubscription.getClientId().equals(clientId))
                .collect(Collectors.toList());

        if (subscriptions.isEmpty()) {
            return;
        }

        DataExportUtil.writeSpaces(xmlStreamWriter, 1);
        xmlStreamWriter.writeStartElement(SubscriptionXML.ROOT_ELEMENT);
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeCharacters("\n");

        for (final @NotNull ChunkSubscription subscription : subscriptions) {
            writeSubscription(xmlStreamWriter, subscription);
        }

        DataExportUtil.writeSpaces(xmlStreamWriter, 1);
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeCharacters("\n");

    }


    private void writeQueuedMessagesInfo(final @NotNull XMLStreamWriter xmlStreamWriter,
                                         final @NotNull String clientID,
                                         final @NotNull List<ChunkClientMessage> allClientMessages,
                                         final @NotNull List<ChunkMsgStore> messages) throws XMLStreamException {

        final @NotNull List<ChunkClientMessage> clientMessages = allClientMessages.stream().filter(msg -> msg.getClientId().equals(clientID)).collect(Collectors.toList());

        if (clientMessages.isEmpty()) {
            return;
        }

        DataExportUtil.writeSpaces(xmlStreamWriter, 1);
        xmlStreamWriter.writeStartElement(QueuedMessageXML.ROOT_ELEMENT);
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeCharacters("\n");

        final @NotNull List<ChunkClientMessage> orderedMessages = clientMessages.stream().
                sorted(Comparator.comparingInt(ChunkClientMessage::getMid)).collect(Collectors.toList());

        for (final @NotNull ChunkClientMessage clientMessage : orderedMessages) {
            writeQueued(xmlStreamWriter, clientMessage, Objects.requireNonNull(messages.stream().filter(chunkMsgStore -> clientMessage.getStoreId() == chunkMsgStore.getStoreId()).findFirst().orElse(null)));
        }

        DataExportUtil.writeSpaces(xmlStreamWriter, 1);
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeCharacters("\n");

    }


    private void writeQueued(final @NotNull XMLStreamWriter xmlStreamWriter,
                             final @NotNull ChunkClientMessage clientMessage,
                             final @NotNull ChunkMsgStore message) throws XMLStreamException {

        DataExportUtil.writeSpaces(xmlStreamWriter, 2);
        xmlStreamWriter.writeStartElement(QueuedMessageXML.QUEUED_MESSAGE_ELEMENT);
        xmlStreamWriter.writeCharacters("\n");

        DataExportUtil.writeNumber(xmlStreamWriter, System.currentTimeMillis(), EXPORTED_AT, 3);
        DataExportUtil.writeString(xmlStreamWriter, QueuedMessageXML.QueuedMessageType.PUBLISH.getName(), QueuedMessageXML.TYPE, 3);
        DataExportUtil.writeNumber(xmlStreamWriter, 0, MessageXML.PACKET_ID, 3);
        DataExportUtil.writeBoolean(xmlStreamWriter, message.getRetain(), QueuedMessageXML.FROM_RETAINED_MESSAGE, 3);
        DataExportUtil.writeNumber(xmlStreamWriter, message.getStoreId(), MessageXML.PUBLISH_ID, 3);
        DataExportUtil.writeString(xmlStreamWriter, "MOSQU", MessageXML.CLUSTER_ID, 3);

        final boolean mustEncode = DataExportUtil.mustEncode(message.getTopic());
        DataExportUtil.writeBoolean(xmlStreamWriter, mustEncode, MessageXML.TOPIC_BASE_64, 3);
        if (mustEncode) {
            DataExportUtil.writeStringEncoded(xmlStreamWriter, message.getTopic(), MessageXML.TOPIC, 3);
        } else {
            DataExportUtil.writeString(xmlStreamWriter, message.getTopic(), MessageXML.TOPIC, 3);
        }

        DataExportUtil.writeStringEncoded(xmlStreamWriter, message.getResponseTopic(), MessageXML.RESPONSE_TOPIC, 3);
        DataExportUtil.writeStringEncoded(xmlStreamWriter, message.getContentType(), MessageXML.CONTENT_TYPE, 3);
        DataExportUtil.writeBytes(xmlStreamWriter, message.getPayload(), MessageXML.MESSAGE, 3);
        DataExportUtil.writeBytes(xmlStreamWriter, message.getCorrelationData(), MessageXML.CORRELATION_DATA, 3);
        DataExportUtil.writeBoolean(xmlStreamWriter, message.getRetain(), MessageXML.RETAINED, 3);
        DataExportUtil.writeBoolean(xmlStreamWriter, clientMessage.getRetainDuplicate(), MessageXML.DUPLICATE_DELIVERY, 3);
        DataExportUtil.writeNumber(xmlStreamWriter, timestamp, MessageXML.TIMESTAMP, 3);
        DataExportUtil.writeNumber(xmlStreamWriter, clientMessage.getQos(), MessageXML.QOS, 3);
        DataExportUtil.writeNumber(xmlStreamWriter, (message.getExpiryTime() == 0) ? 4_294_967_296L : (message.getExpiryTime() - timestamp / 1000), MessageXML.MESSAGE_EXPIRY, 3);
        DataExportUtil.writeNumber(xmlStreamWriter, message.getPayloadFormatIndicator() != null ? message.getPayloadFormatIndicator().getCode() : null, MessageXML.PAYLOAD_FORMAT_INDICATOR, 3);
        DataExportUtil.writeUserProperties(xmlStreamWriter, message.getUserProperties(), 3);
        DataExportUtil.writeSubscriptionIdentifiers(xmlStreamWriter, clientMessage.getSubscriptionIdentifier(), 3);

        DataExportUtil.writeSpaces(xmlStreamWriter, 2);
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeCharacters("\n");

    }

    private void writeClientSessionAttributes(final @NotNull XMLStreamWriter xmlStreamWriter,
                                              final @NotNull ChunkClient client,
                                              final long timestamp) throws XMLStreamException {

        final boolean mustEncode = DataExportUtil.mustEncode(client.getClientId());
        xmlStreamWriter.writeAttribute(ClientSessionXML.CLIENT_ID_BASE_64, String.valueOf(mustEncode));
        if (mustEncode) {
            xmlStreamWriter.writeAttribute(ClientSessionXML.CLIENT_ID, BASE_64.encode(client.getClientId().getBytes(UTF_8)));
        } else {
            xmlStreamWriter.writeAttribute(ClientSessionXML.CLIENT_ID, client.getClientId());
        }
        if (!client.isConnected()) {
            xmlStreamWriter.writeAttribute(ClientSessionXML.DISCONNECTED_SINCE, String.valueOf(timestamp));
        }
        xmlStreamWriter.writeAttribute(ClientSessionXML.SESSION_EXPIRY, String.valueOf(client.getSessionExpiryInterval()));
        xmlStreamWriter.writeAttribute(GlobalXML.HIVEMQ_VERSION, hiveMqVersion);
        xmlStreamWriter.writeAttribute(EXPORTED_AT, String.valueOf(System.currentTimeMillis()));

        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeCharacters("\n");
    }

    private void writeSubscription(final @NotNull XMLStreamWriter xmlStreamWriter, final @NotNull ChunkSubscription subscription) throws XMLStreamException {

        DataExportUtil.writeSpaces(xmlStreamWriter, 2);
        xmlStreamWriter.writeStartElement(SubscriptionXML.SUBSCRIPTION_ELEMENT);
        xmlStreamWriter.writeCharacters("\n");

        DataExportUtil.writeNumber(xmlStreamWriter, System.currentTimeMillis(), EXPORTED_AT, 3);

        final boolean mustEncode = DataExportUtil.mustEncode(subscription.getTopic());
        DataExportUtil.writeBoolean(xmlStreamWriter, mustEncode, MessageXML.TOPIC_BASE_64, 3);
        if (mustEncode) {
            DataExportUtil.writeStringEncoded(xmlStreamWriter, subscription.getTopic(), MessageXML.TOPIC, 3);
        } else {
            DataExportUtil.writeString(xmlStreamWriter, subscription.getTopic(), MessageXML.TOPIC, 3);
        }

        DataExportUtil.writeNumber(xmlStreamWriter, subscription.getQos(), MessageXML.QOS, 3);
        DataExportUtil.writeBoolean(xmlStreamWriter, subscription.isNoLocal(), SubscriptionXML.NO_LOCAL, 3);
        DataExportUtil.writeBoolean(xmlStreamWriter, subscription.isRetainAsPublished(), SubscriptionXML.RETAIN_AS_PUBLISHED, 3);
        DataExportUtil.writeNumber(xmlStreamWriter, subscription.getRetainHandling(), SubscriptionXML.RETAIN_HANDLING, 3);
        DataExportUtil.writeNumber(xmlStreamWriter, (subscription.getIdentifier() == 0) ? null : subscription.getIdentifier(), MessageXML.SUBSCRIPTION_IDENTIFIER, 3);

        DataExportUtil.writeSpaces(xmlStreamWriter, 2);
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
        xmlStreamWriter.writeCharacters("\n");

    }
}
