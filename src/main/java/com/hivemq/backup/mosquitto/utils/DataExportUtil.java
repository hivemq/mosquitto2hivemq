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
package com.hivemq.backup.mosquitto.utils;

import com.google.common.io.BaseEncoding;
import com.hivemq.backup.mosquitto.db.Property;
import com.hivemq.backup.mosquitto.format.MessageXML;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.File;
import java.util.List;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author Florian Limpöck
 * @author Lukas Brand
 * @since 1.0.0
 */
public class DataExportUtil {

    public static final @NotNull BaseEncoding BASE_64 = BaseEncoding.base64();
    @SuppressWarnings("SpellCheckingInspection")
    private static final @NotNull String VALID_CHARS = "01234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ-_=!/\\'.,?()[]{}%$§*+\"'#@€`´";

    //do not instantiate
    private DataExportUtil() {
    }

    public static void writeString(final @NotNull XMLStreamWriter xmlStreamWriter,
                                   final @Nullable String value,
                                   final @NotNull String element,
                                   final int depth) throws XMLStreamException {
        if (value == null) {
            return;
        }
        writeSpaces(xmlStreamWriter, depth);
        xmlStreamWriter.writeStartElement(element);
        xmlStreamWriter.writeCharacters(value);
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
    }

    public static void writeStringEncoded(final @NotNull XMLStreamWriter xmlStreamWriter,
                                          final @Nullable String value,
                                          final @NotNull String element,
                                          final int depth) throws XMLStreamException {
        if (value == null) {
            return;
        }
        writeSpaces(xmlStreamWriter, depth);
        xmlStreamWriter.writeStartElement(element);
        xmlStreamWriter.writeCharacters(BASE_64.encode(value.getBytes(UTF_8)));
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
    }

    public static void writeNumber(final @NotNull XMLStreamWriter xmlStreamWriter,
                                   final @Nullable Number value,
                                   final @NotNull String element,
                                   final int depth) throws XMLStreamException {
        if (value == null) {
            return;
        }
        writeSpaces(xmlStreamWriter, depth);
        xmlStreamWriter.writeStartElement(element);
        xmlStreamWriter.writeCharacters(String.valueOf(value));
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
    }

    public static void writeBoolean(final @NotNull XMLStreamWriter xmlStreamWriter,
                                    final boolean value,
                                    final @NotNull String element,
                                    final int depth) throws XMLStreamException {
        writeSpaces(xmlStreamWriter, depth);
        xmlStreamWriter.writeStartElement(element);
        xmlStreamWriter.writeCharacters(String.valueOf(value));
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
    }

    public static void writeBytes(final @NotNull XMLStreamWriter xmlStreamWriter,
                                  final @Nullable byte[] value,
                                  final @NotNull String element,
                                  final int depth) throws XMLStreamException {
        if (value == null) {
            return;
        }
        writeSpaces(xmlStreamWriter, depth);
        xmlStreamWriter.writeStartElement(element);
        xmlStreamWriter.writeCharacters(BASE_64.encode(value));
        xmlStreamWriter.writeEndElement();
        xmlStreamWriter.writeCharacters("\n");
    }

    public static void writeUserProperties(final @NotNull XMLStreamWriter xmlStreamWriter,
                                           final @NotNull List<Property> userProperties,
                                           final int depth) throws XMLStreamException {
        if (userProperties.size() > 0) {
            writeSpaces(xmlStreamWriter, depth);
            xmlStreamWriter.writeStartElement(MessageXML.USER_PROPERTIES);
            xmlStreamWriter.writeCharacters("\n");

            for (final @NotNull Property mqttUserProperty : userProperties) {
                writeSpaces(xmlStreamWriter, depth + 1);
                xmlStreamWriter.writeStartElement(MessageXML.USER_PROPERTY);
                xmlStreamWriter.writeCharacters("\n");

                writeStringEncoded(xmlStreamWriter, mqttUserProperty.getKey(), MessageXML.USER_PROPERTY_NAME, depth + 2);
                writeStringEncoded(xmlStreamWriter, new String(Objects.requireNonNull(mqttUserProperty.getByteArrayValue())), MessageXML.USER_PROPERTY_VALUE, depth + 2);

                writeSpaces(xmlStreamWriter, depth + 1);
                xmlStreamWriter.writeEndElement();
                xmlStreamWriter.writeCharacters("\n");
            }

            writeSpaces(xmlStreamWriter, depth);
            xmlStreamWriter.writeEndElement();
            xmlStreamWriter.writeCharacters("\n");
        }
    }

    public static void writeSubscriptionIdentifiers(final @NotNull XMLStreamWriter xmlStreamWriter,
                                                    final @Nullable List<Integer> subscriptionIdentifiers,
                                                    final int depth) throws XMLStreamException {
        if (subscriptionIdentifiers != null && subscriptionIdentifiers.size() > 0) {
            writeSpaces(xmlStreamWriter, depth);
            xmlStreamWriter.writeStartElement(MessageXML.SUBSCRIPTION_IDENTIFIERS);
            xmlStreamWriter.writeCharacters("\n");

            for (final @NotNull Integer identifier : subscriptionIdentifiers) {
                writeNumber(xmlStreamWriter, (identifier == 0) ? null : identifier, MessageXML.SUBSCRIPTION_IDENTIFIER, depth + 1);
            }

            writeSpaces(xmlStreamWriter, depth);
            xmlStreamWriter.writeEndElement();
            xmlStreamWriter.writeCharacters("\n");
        }
    }

    public static void writeSpaces(final @NotNull XMLStreamWriter xmlStreamWriter, final int depth) throws XMLStreamException {
        for (int x = 0; x < depth; x++) {
            xmlStreamWriter.writeCharacters("    ");
        }
    }

    public static boolean mustEncode(final @NotNull String string) {
        return !StringUtils.containsOnly(string, VALID_CHARS);
    }


    public static @NotNull String getNextFileName(final @NotNull File folder, final @NotNull String identifier) {

        final @NotNull StringBuilder fileNameBuilder = new StringBuilder();
        for (int i = 0; i < identifier.length(); i++) {
            final char c = identifier.charAt(i);
            if (isSafeChar(c)) {
                fileNameBuilder.append(c);
            } else {
                fileNameBuilder.append('-');
            }
        }

        final @NotNull String fileNameClientId = fileNameBuilder.toString();

        String finalFileName = null;
        for (int i = 0; finalFileName == null; i++) {
            if (new @NotNull File(folder, fileNameClientId + "-" + i).exists()) {
                continue;
            }
            finalFileName = fileNameClientId + "-" + i;
        }

        return finalFileName;
    }

    private static boolean isSafeChar(final char c) {
        return Character.isJavaIdentifierPart(c) || c == '-';
    }
}
