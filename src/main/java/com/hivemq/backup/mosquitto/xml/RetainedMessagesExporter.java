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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.hivemq.backup.mosquitto.db.ChunkMsgStore;
import com.hivemq.backup.mosquitto.format.GlobalXML;
import com.hivemq.backup.mosquitto.format.MessageXML;
import com.hivemq.backup.mosquitto.format.RetainedMessageXML;
import com.hivemq.backup.mosquitto.utils.DataExportUtil;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import static com.hivemq.backup.mosquitto.format.GlobalXML.EXPORTED_AT;

/**
 * @author Florian Limpöck
 * @since 1.0.0
 */

public class RetainedMessagesExporter {

    private final @NotNull XMLOutputFactory factory = XMLOutputFactory.newInstance();
    private final long timestamp;
    private final @NotNull Path fileSaveLocation;
    private final @NotNull String clusterId;
    private final @NotNull String hiveMqVersion;
    private final int maxFileSize;

    private int filesWritten = 0;
    private int filesDone = 0;

    private @Nullable XMLStreamWriter xmlStreamWriter;
    private FileOutputStream fileOutputStream;

    /**
     * Creates a RetainedMessagesExporter.
     *
     * @param timestamp        Creation timestamp.
     * @param fileSaveLocation The folder the retained messages get saved to.
     * @param clusterId        The HiveMQ cluster id.
     * @param hiveMqVersion    The used HiveMQ version.
     * @param maxFileSize      The maximum file size for a retained message XML file.
     */
    public RetainedMessagesExporter(final long timestamp,
                                    final @NotNull Path fileSaveLocation,
                                    final @NotNull String clusterId,
                                    final @NotNull String hiveMqVersion,
                                    final int maxFileSize) {
        this.timestamp = timestamp;
        this.fileSaveLocation = fileSaveLocation;
        this.clusterId = clusterId;
        this.hiveMqVersion = hiveMqVersion;
        this.maxFileSize = maxFileSize;
    }

    /**
     * Writes all retained messages to XML.
     *
     * @param retainedMessages All retained messages.
     */
    @VisibleForTesting
    public void writeToXml(final @Nullable List<ChunkMsgStore> retainedMessages) {

        if (retainedMessages == null || retainedMessages.size() == 0) {
            return;
        }

        final @NotNull File retainedFolder = new File(fileSaveLocation.toFile(), "retained-messages");
        final @NotNull String filePrefix = clusterId + "-retained-messages-";

        try {
            int index = ++filesWritten;
            //check if written files equals the amount of done files, then create new stream writer
            if (filesDone == index - 1) {

                FileUtils.forceMkdir(retainedFolder);

                fileOutputStream = new FileOutputStream(new File(retainedFolder, filePrefix + index + ".xml"));

                xmlStreamWriter = factory.createXMLStreamWriter(fileOutputStream);
                xmlStreamWriter.writeStartDocument(); //  <?xml version="1.0" ?>
                xmlStreamWriter.writeCharacters("\n");

                xmlStreamWriter.writeStartElement(RetainedMessageXML.ROOT_ELEMENT);
                xmlStreamWriter.writeAttribute(GlobalXML.HIVEMQ_VERSION, hiveMqVersion);
                xmlStreamWriter.writeAttribute(EXPORTED_AT, String.valueOf(timestamp));
                xmlStreamWriter.writeCharacters("\n");

            } else {
                //decrease file count again since the current is not full yet.
                index = --filesWritten;
            }

            Preconditions.checkNotNull(fileOutputStream, "The output stream must never be null here");
            Preconditions.checkNotNull(xmlStreamWriter, "The stream writer must never be null here");

            for (final @Nullable ChunkMsgStore retainedMessage : retainedMessages) {

                //we don't need tombstones.
                if (retainedMessage == null /*|| retainedMessage.isDeleted()*/) {
                    // we don't want to iterate over tombstones again
                    continue;
                }

                writeRetainedMessage(Objects.requireNonNull(xmlStreamWriter), retainedMessage, timestamp);

                final @NotNull File file = new File(retainedFolder, filePrefix + index + ".xml");

                if (file.length() > maxFileSize) {
                    xmlStreamWriter.writeCharacters("\n");
                    xmlStreamWriter.writeEndElement();
                    xmlStreamWriter.close();
                    fileOutputStream.close();
                    filesDone = index;
                    if (retainedMessages.isEmpty()) {
                        return;
                    }
                    writeToXml(retainedMessages);
                    break;
                }
            }
            xmlStreamWriter.writeCharacters("\n");
            xmlStreamWriter.writeEndElement();
            xmlStreamWriter.close();
            fileOutputStream.close();

        } catch (final @NotNull Exception ex) {
            ex.printStackTrace();
            if (xmlStreamWriter != null && fileOutputStream != null) {
                try {
                    xmlStreamWriter.close();
                } catch (final XMLStreamException e) {
                    e.printStackTrace();
                }
                try {
                    fileOutputStream.close();
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void writeRetainedMessage(final @NotNull XMLStreamWriter xmlStreamWriter, final @NotNull ChunkMsgStore retainedMessage, final long timestamp) throws XMLStreamException {

        xmlStreamWriter.writeCharacters("\n");
        DataExportUtil.writeSpaces(xmlStreamWriter, 1);
        xmlStreamWriter.writeStartElement(RetainedMessageXML.RETAINED_MESSAGE_ELEMENT);
        xmlStreamWriter.writeCharacters("\n");

        DataExportUtil.writeNumber(xmlStreamWriter, timestamp, EXPORTED_AT, 2);

        final boolean mustEncode = DataExportUtil.mustEncode(retainedMessage.getTopic());
        DataExportUtil.writeBoolean(xmlStreamWriter, mustEncode, MessageXML.TOPIC_BASE_64, 2);
        if (mustEncode) {
            DataExportUtil.writeStringEncoded(xmlStreamWriter, retainedMessage.getTopic(), MessageXML.TOPIC, 2);
        } else {
            DataExportUtil.writeString(xmlStreamWriter, retainedMessage.getTopic(), MessageXML.TOPIC, 2);
        }

        DataExportUtil.writeNumber(xmlStreamWriter, timestamp, MessageXML.TIMESTAMP, 2);
        DataExportUtil.writeBytes(xmlStreamWriter, retainedMessage.getPayload(), MessageXML.MESSAGE, 2);
        DataExportUtil.writeNumber(xmlStreamWriter, retainedMessage.getQos(), MessageXML.QOS, 2);
        DataExportUtil.writeNumber(xmlStreamWriter, (retainedMessage.getExpiryTime() == 0) ? 4_294_967_296L : (retainedMessage.getExpiryTime() - timestamp / 1000), MessageXML.MESSAGE_EXPIRY, 2);
        DataExportUtil.writeStringEncoded(xmlStreamWriter, retainedMessage.getContentType(), MessageXML.CONTENT_TYPE, 2);
        DataExportUtil.writeStringEncoded(xmlStreamWriter, retainedMessage.getResponseTopic(), MessageXML.RESPONSE_TOPIC, 2);
        DataExportUtil.writeBytes(xmlStreamWriter, retainedMessage.getCorrelationData(), MessageXML.CORRELATION_DATA, 2);
        DataExportUtil.writeNumber(xmlStreamWriter, retainedMessage.getPayloadFormatIndicator() != null ? retainedMessage.getPayloadFormatIndicator().getCode() : null, MessageXML.PAYLOAD_FORMAT_INDICATOR, 2);
        DataExportUtil.writeUserProperties(xmlStreamWriter, retainedMessage.getUserProperties(), 2);

        DataExportUtil.writeSpaces(xmlStreamWriter, 1);
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");

    }

}
