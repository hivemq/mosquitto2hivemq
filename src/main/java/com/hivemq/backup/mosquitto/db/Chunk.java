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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * @author Lukas Brand
 * @since 1.0.0
 */
public class Chunk {

    /**
     * Contains all Configuration-Chunks.
     */
    private final @NotNull List<ChunkCfg> chunkCfgs = new ArrayList<>();

    /**
     * Contains all MessageStore-Chunks.
     */
    private final @NotNull List<ChunkMsgStore> chunkMsgStores = new ArrayList<>();

    /**
     * Contains all Client-Chunks.
     */
    private final @NotNull List<ChunkClient> chunkClients = new ArrayList<>();

    /**
     * Contains all ClientMessage-Chunks.
     */
    private final @NotNull List<ChunkClientMessage> chunkClientMessages = new ArrayList<>();

    /**
     * Contains all Subscription-Chunks.
     */
    private final @NotNull List<ChunkSubscription> chunkSubscriptions = new ArrayList<>();

    /**
     * Contains all Retain-Chunks.
     */
    private final @NotNull List<ChunkRetain> chunkRetains = new ArrayList<>();

    /**
     * Enables force mode to ignore migration failures.
     */
    private final boolean forceCreationWithFailures;

    /**
     * Default Constructor.
     *
     * @hidden
     */
    public Chunk(final boolean forceCreationWithFailures) {

        this.forceCreationWithFailures = forceCreationWithFailures;
    }

    /**
     * Creates a Byte Array which contains the whole Mosquitto database File.
     *
     * @param filePath The Path to the .db File created by Mosquitto.
     * @return Returns a Byte Array containing all persistent chunks created by Mosquitto.
     * @throws IOException Throws an IOException if the File cannot be found.
     */
    @NotNull
    public byte[] readMosquittoDbFile(final @NotNull Path filePath) throws IOException {
        return Files.readAllBytes(filePath);
    }

    /**
     * Reads the database Chunks and saves it to individual Lists.
     * <p></p>
     * A Chunk always contains its identifier and its length as well as individual content. <br>
     * There are 6 types of Chunks: <br>
     * - Configuration-Chunk <br>
     * - MessageStore-Chunk <br>
     * - Client-Chunk <br>
     * - ClientMessage-Chunk <br>
     * - Subscription-Chunk <br>
     * - Retain-Chunk <br>
     *
     * @param dbBytes       Byte Array containing all persistent Chunks created by Mosquitto.
     * @param displayChunks Enables a detailed printout of useful information about the process.
     * @throws IllegalArgumentException Throws an IllegalArgumentException if the databases binary header does not match with the default header.
     */
    public void createChunksFromBinary(final @NotNull byte[] dbBytes, final boolean displayChunks) throws IllegalArgumentException {
        final @NotNull ByteBuffer byteBuffer = ByteBuffer.wrap(dbBytes);

        //MAGIC HEADER:
        final @NotNull byte[] fileIdentifierHeader = {0x00, (byte) 0xB5, 0x00, 'm', 'o', 's', 'q', 'u', 'i', 't', 't', 'o', ' ', 'd', 'b'};
        final @NotNull byte[] header = Arrays.copyOfRange(byteBuffer.array(), 0, 15);
        if (Arrays.equals(fileIdentifierHeader, header)) {
            if (displayChunks) {
                Logger.info("Magic header are equal.");
            }
        } else {
            if (!forceCreationWithFailures) {
                throw new IllegalArgumentException("Unsupported Database File. File header does not match. Exiting.");
            } else {
                Logger.error("Unsupported Database File. File header does not match.");
            }
        }

        //CRC HEADER:
        int arrayIndex = 15;
        if (displayChunks) {
            Logger.info("CRC: " + getCrc(byteBuffer, arrayIndex));
        }
        arrayIndex += Integer.BYTES;

        //DB VERSION HEADER:
        if (displayChunks) {
            Logger.info("DB-Version: " + getDbVersion(byteBuffer, arrayIndex));
        }
        arrayIndex += Integer.BYTES;

        //Get Chunks:
        while (arrayIndex < dbBytes.length) {
            switch (Objects.requireNonNull(getChunkType(byteBuffer, arrayIndex))) {
                case DB_CHUNK_CFG:
                    arrayIndex += Integer.BYTES;
                    final int cfgLength = getChunkLength(byteBuffer, arrayIndex);
                    arrayIndex += Integer.BYTES;
                    getCfgChunk(byteBuffer, arrayIndex);
                    arrayIndex += cfgLength;
                    break;

                case DB_CHUNK_MSG_STORE:
                    arrayIndex += Integer.BYTES;
                    final int msgStoreLength = getChunkLength(byteBuffer, arrayIndex);
                    arrayIndex += Integer.BYTES;
                    getMsgStoreChunk(byteBuffer, arrayIndex, msgStoreLength);
                    arrayIndex += msgStoreLength;
                    break;

                case DB_CHUNK_CLIENT_MSG:
                    arrayIndex += Integer.BYTES;
                    final int clientMsgLength = getChunkLength(byteBuffer, arrayIndex);
                    arrayIndex += Integer.BYTES;
                    getClientMessageChunk(byteBuffer, arrayIndex, clientMsgLength);
                    arrayIndex += clientMsgLength;
                    break;

                case DB_CHUNK_RETAIN:
                    arrayIndex += Integer.BYTES;
                    final int retainLength = getChunkLength(byteBuffer, arrayIndex);
                    arrayIndex += Integer.BYTES;
                    getRetainChunk(byteBuffer, arrayIndex);
                    arrayIndex += retainLength;
                    break;

                case DB_CHUNK_SUB:
                    arrayIndex += Integer.BYTES;
                    final int subLength = getChunkLength(byteBuffer, arrayIndex);
                    arrayIndex += Integer.BYTES;
                    getSubscriptionChunk(byteBuffer, arrayIndex);
                    arrayIndex += subLength;
                    break;

                case DB_CHUNK_CLIENT:
                    arrayIndex += Integer.BYTES;
                    final int clientLength = getChunkLength(byteBuffer, arrayIndex);
                    arrayIndex += Integer.BYTES;
                    getClientChunk(byteBuffer, arrayIndex);
                    arrayIndex += clientLength;
                    break;

                default:
                    if (!forceCreationWithFailures) {
                        throw new IllegalArgumentException("Corrupted Chunk occurred. Exiting.");
                    } else {
                        Logger.error("Corrupted Chunk occurred.");
                    }
                    break;
            }
        }

        if (displayChunks) {
            Logger.info("Binary mosquitto.db file:");
            StringBuilder stringBuilder = new StringBuilder();
            for (final byte b : dbBytes) {
                //String st = String.format("%02X", b);
                final String st = String.format("%d", b);
                stringBuilder.append(st).append(" ");
            }
            Logger.info(stringBuilder.toString());
            Logger.info("Detected Chunks:");
            chunkCfgs.forEach(Logger::info);
            chunkMsgStores.forEach(Logger::info);
            chunkClientMessages.forEach(Logger::info);
            chunkRetains.forEach(Logger::info);
            chunkSubscriptions.forEach(Logger::info);
            chunkClients.forEach(Logger::info);
        }
    }

    /**
     * Adds a Configuration-Chunk to the Configuration-Chunk List.
     * <p></p>
     * The Chunk contains in order: <br>
     * - a last database id (long, encoded little endian) <br>
     * - a shutdown value (byte) <br>
     * - the size of the database id (byte)
     *
     * @param cfgBytes Byte Array containing all persistent Chunks created by Mosquitto.
     * @param index    The index of the Configuration-Chunk after its length attribute.
     */
    private void getCfgChunk(final @NotNull ByteBuffer cfgBytes, final int index) {
        int chunkIndex = index;
        final long lastDbId = cfgBytes.duplicate().order(ByteOrder.LITTLE_ENDIAN).getLong(chunkIndex);
        chunkIndex += Long.BYTES;
        final byte shutdown = cfgBytes.get(chunkIndex);
        chunkIndex += Byte.BYTES;
        final byte dbIdSize = cfgBytes.get(chunkIndex);
        chunkCfgs.add(new ChunkCfg(shutdown, dbIdSize, lastDbId));
    }

    /**
     * Adds a Subscription-Chunk to the Subscription-Chunk List.
     * <p></p>
     * The Chunk contains in order: <br>
     * - a subscription identifier (integer) <br>
     * - the string length of the subscribing client id [Mosquitto] (short) <br>
     * - the string length of the subscribed topic [Mosquitto] (short) <br>
     * - the quality of service of the subscription (byte, enum) <br>
     * - an options byte containing noLocal, isRetainAsPublished and retainHandling (byte) <br>
     * - the subscribing client id (String) <br>
     * - the subscribed topic (String)
     *
     * @param subBytes Byte Array containing all persistent Chunks created by Mosquitto.
     * @param index    The index of the Subscription-Chunk after its length attribute.
     */
    private void getSubscriptionChunk(final @NotNull ByteBuffer subBytes, final int index) {
        int chunkIndex = index;
        final int identifier = subBytes.getInt(chunkIndex);
        chunkIndex += Integer.BYTES;
        final short idLength = subBytes.getShort(chunkIndex);
        chunkIndex += Short.BYTES;
        final short topicLength = subBytes.getShort(chunkIndex);
        chunkIndex += Short.BYTES;
        final byte qos = subBytes.get(chunkIndex);
        chunkIndex += Byte.BYTES;
        final byte options = subBytes.get(chunkIndex);
        chunkIndex += Byte.BYTES;
        //2 Extra Bytes to keep Alignment.
        chunkIndex += 2;

        final @NotNull String clientId = new String(Arrays.copyOfRange(subBytes.array(), chunkIndex, chunkIndex + idLength));
        chunkIndex += idLength;

        final @NotNull String topic = new String(Arrays.copyOfRange(subBytes.array(), chunkIndex, chunkIndex + topicLength));

        chunkSubscriptions.add(new ChunkSubscription(identifier, qos, options, clientId, topic));
    }

    /**
     * Adds a Retain-Chunk to the Retain-Chunk List.
     * <p></p>
     * The Chunk contains in order: <br>
     * - the store id of a retained message (long, encoded little endian)
     *
     * @param clientBytes Byte Array containing all persistent Chunks created by Mosquitto.
     * @param index       The index of the Retain-Chunk after its length attribute.
     */
    @SuppressWarnings("UnusedAssignment")
    private void getRetainChunk(final @NotNull ByteBuffer clientBytes, final int index) {
        int chunkIndex = index;
        final long storeId = clientBytes.duplicate().order(ByteOrder.LITTLE_ENDIAN).getLong(chunkIndex);
        chunkIndex += Long.BYTES;

        chunkRetains.add(new ChunkRetain(storeId));
    }

    /**
     * Adds a Client-Chunk to the Client-Chunk List.
     * <p></p>
     * The Chunk contains in order: <br>
     * - the time the client session will expire (long, encoded little endian) <br>
     * - the session expiry interval (integer) <br>
     * - the last message id which had an action with the client (short) <br>
     * - the string length of the client id [Mosquitto] (short) <br>
     * - the client id (string) <br>
     *
     * @param clientBytes Byte Array containing all persistent Chunks created by Mosquitto.
     * @param index       The index of the Client-Chunk after its length attribute.
     */
    private void getClientChunk(final @NotNull ByteBuffer clientBytes, final int index) {
        int chunkIndex = index;
        final long sessionExpiryTime = clientBytes.duplicate().order(ByteOrder.LITTLE_ENDIAN).getLong(chunkIndex);
        chunkIndex += Long.BYTES;
        final int sessionExpiryInterval = clientBytes.getInt(chunkIndex);
        chunkIndex += Integer.BYTES;
        final short lastMid = clientBytes.getShort(chunkIndex);
        chunkIndex += Short.BYTES;
        final short idLength = clientBytes.getShort(chunkIndex);
        chunkIndex += Short.BYTES;

        final @NotNull String clientId = new String(Arrays.copyOfRange(clientBytes.array(), chunkIndex, chunkIndex + idLength));

        chunkClients.add(new ChunkClient(clientId, lastMid, sessionExpiryTime, sessionExpiryInterval));
    }

    /**
     * Adds a ClientMessage-Chunk to the ClientMessage-Chunk List.
     * <p></p>
     * The Chunk contains in order: <br>
     * - the store id of a client message (long, encoded little endian) <br>
     * - the last message id, equal to the clients last message id (short) <br>
     * - the string length of the owning client [Mosquitto] (short) <br>
     * - the quality of service of the stored message (byte) <br>
     * - the state of the message [Mosquitto] (byte, enum) <br>
     * - a boolean to check for retained duplicates (byte, boolean) <br>
     * - the message direction (byte) <br>
     * - the owning client id (String) <br>
     * - a list of properties (byte[], List&lt;Property&gt;)
     *
     * @param clientMessageBytes  Byte Array containing all persistent Chunks created by Mosquitto.
     * @param index               The index of the ClientMessage-Chunk after its length attribute.
     * @param clientMessageLength The length of the whole ClientMessage-Chunk.
     */
    private void getClientMessageChunk(final @NotNull ByteBuffer clientMessageBytes, final int index, final int clientMessageLength) {
        int chunkIndex = index;
        final long storeId = clientMessageBytes.duplicate().order(ByteOrder.LITTLE_ENDIAN).getLong(chunkIndex);
        chunkIndex += Long.BYTES;
        final short mid = clientMessageBytes.getShort(chunkIndex);
        chunkIndex += Short.BYTES;
        final short idLength = clientMessageBytes.getShort(chunkIndex);
        chunkIndex += Short.BYTES;
        final byte qos = clientMessageBytes.get(chunkIndex);
        chunkIndex += Byte.BYTES;
        final byte state = clientMessageBytes.get(chunkIndex);
        chunkIndex += Byte.BYTES;
        final byte retainDuplicate = clientMessageBytes.get(chunkIndex);
        chunkIndex += Byte.BYTES;
        final byte direction = clientMessageBytes.get(chunkIndex);
        chunkIndex += Byte.BYTES;

        final @NotNull String clientId = new String(Arrays.copyOfRange(clientMessageBytes.array(), chunkIndex, chunkIndex + idLength));
        chunkIndex += idLength;

        final int absoluteLength = index + clientMessageLength;
        final @NotNull List<Property> properties = getProperties(clientMessageBytes, chunkIndex, absoluteLength);

        chunkClientMessages.add(new ChunkClientMessage(storeId, mid, qos, state, retainDuplicate, direction, clientId, properties));
    }

    /**
     * Adds a MessageStore-Chunk to the MessageStore-Chunk List.
     * <p></p>
     * the Chunk contains in order: <br>
     * - the store id of a message (long, encoded little endian) <br>
     * - the message expiry time in seconds (long, encoded little endian) <br>
     * - the content/payload length (integer) <br>
     * - the source message id [Mosquitto] (short) <br>
     * - the source string id length [Mosquitto] (short) <br>
     * - the source string username length [Mosquitto] (short) <br>
     * - the topic string length [Mosquitto] (short) <br>
     * - the source port [Mosquitto] (short) <br>
     * - the quality of service of the stored message (byte, enum) <br>
     * - a boolean whether the message is retained or not (byte, boolean) <br>
     * - either a username or id string, dependent on the username or id length (String) <br>
     * - the topic the message was sent to (String) <br>
     * - the content/payload (String) <br>
     * - a list of properties (byte[], List&lt;Property&gt;) <br>
     *
     * @param msgStoreBytes  Byte Array containing all persistent Chunks created by Mosquitto.
     * @param index          The index of the MessageStore-Chunk after its length attribute.
     * @param msgStoreLength The length of the whole MessageStore-Chunk.
     */
    private void getMsgStoreChunk(final @NotNull ByteBuffer msgStoreBytes, final int index, final int msgStoreLength) {
        int chunkIndex = index;
        final long storeId = msgStoreBytes.duplicate().order(ByteOrder.LITTLE_ENDIAN).getLong(chunkIndex);
        chunkIndex += Long.BYTES;
        final long expiryTime = msgStoreBytes.duplicate().order(ByteOrder.LITTLE_ENDIAN).getLong(chunkIndex);
        chunkIndex += Long.BYTES;
        final int payloadLength = msgStoreBytes.getInt(chunkIndex);
        chunkIndex += Integer.BYTES;
        final short sourceMid = msgStoreBytes.getShort(chunkIndex);
        chunkIndex += Short.BYTES;
        final short sourceIdLength = msgStoreBytes.getShort(chunkIndex);
        chunkIndex += Short.BYTES;
        final short sourceUsernameLength = msgStoreBytes.getShort(chunkIndex);
        chunkIndex += Short.BYTES;
        final short topicLength = msgStoreBytes.getShort(chunkIndex);
        chunkIndex += Short.BYTES;
        final short sourcePort = msgStoreBytes.getShort(chunkIndex);
        chunkIndex += Short.BYTES;
        final byte qos = msgStoreBytes.get(chunkIndex);
        chunkIndex += Byte.BYTES;
        final byte retain = msgStoreBytes.get(chunkIndex);
        chunkIndex += Byte.BYTES;

        final @NotNull String usernameOrId;
        if (sourceIdLength > 0) {
            usernameOrId = new String(Arrays.copyOfRange(msgStoreBytes.array(), chunkIndex, chunkIndex + sourceIdLength));
            chunkIndex += sourceIdLength;
        } else if (sourceUsernameLength > 0) {
            usernameOrId = new String(Arrays.copyOfRange(msgStoreBytes.array(), chunkIndex, chunkIndex + sourceUsernameLength));
            chunkIndex += sourceUsernameLength;
        } else {
            usernameOrId = "";
        }

        final @NotNull String topic = new String(Arrays.copyOfRange(msgStoreBytes.array(), chunkIndex, chunkIndex + topicLength));
        chunkIndex += topicLength;

        final @NotNull String payload;
        if (payloadLength > 0) {
            payload = new String(Arrays.copyOfRange(msgStoreBytes.array(), chunkIndex, chunkIndex + payloadLength));
            chunkIndex += payloadLength;
        } else {
            payload = "";
        }

        final int absoluteLength = index + msgStoreLength;
        final @NotNull List<Property> properties = getProperties(msgStoreBytes, chunkIndex, absoluteLength);

        chunkMsgStores.add(new ChunkMsgStore(storeId, sourcePort, sourceMid, topic, qos, retain, usernameOrId, payloadLength, expiryTime, payload, properties));
    }

    /**
     * Obtains the Properties from the given Byte Array.
     * <p></p>
     * A Property always contains its identifier and its length as well as individual content. <br>
     * There are 6 types of Properties: <br>
     * - Payload Format Indicator Property (byte, enum)
     * - Subscription Identifier Property (varInt)
     * - Content Type Property (short [string length], String)
     * - Response Topic Property (short [string length], String)
     * - Correlation Data Property (short [byte array length], byte[])
     * - User Property Property (short [key length], String key, short [value length], String value)
     *
     * @param propertiesBytes Byte Array containing all persistent Chunks created by Mosquitto.
     * @param chunkIndex      The index of the Properties in the Chunk (including length of Properties).
     * @param absoluteLength  The length of the whole Chunk.
     * @return Returns an unmodifiable List of Properties.
     */
    private @NotNull List<Property> getProperties(@NotNull ByteBuffer propertiesBytes, int chunkIndex, int absoluteLength) {

        final @NotNull List<Property> properties = new ArrayList<>();
        if (chunkIndex < absoluteLength) /* Checks for Properties */ {

            int remainingLength = absoluteLength - chunkIndex;
            int propertiesIndex = chunkIndex;
            final int propertiesLength = Property.readVarInt(Arrays.copyOfRange(propertiesBytes.array(), propertiesIndex, propertiesIndex + remainingLength));
            propertiesIndex += Property.getVarIntLength();

            while (propertiesLength > propertiesIndex - chunkIndex) {
                @Nullable PropertyType type = PropertyType.getType(Property.readVarInt(Arrays.copyOfRange(propertiesBytes.array(), propertiesIndex, absoluteLength)));
                propertiesIndex += Property.getVarIntLength();
                switch (Objects.requireNonNull(type)) {
                    case MQTT_PROP_PAYLOAD_FORMAT_INDICATOR:
                        final byte byteProperty = propertiesBytes.get(propertiesIndex);
                        propertiesIndex += Byte.BYTES;
                        properties.add(new Property(type, byteProperty));
                        break;

                    case MQTT_PROP_SUBSCRIPTION_IDENTIFIER:
                        final int varIntProperty = Property.readVarInt(Arrays.copyOfRange(propertiesBytes.array(), propertiesIndex, absoluteLength));
                        propertiesIndex += Property.getVarIntLength();
                        properties.add(new Property(type, varIntProperty));
                        break;

                    case MQTT_PROP_CONTENT_TYPE:
                    case MQTT_PROP_RESPONSE_TOPIC:
                        final short stringLength = propertiesBytes.getShort(propertiesIndex);
                        propertiesIndex += Short.BYTES;
                        final @NotNull String stringProperty = new String(Arrays.copyOfRange(propertiesBytes.array(), propertiesIndex, propertiesIndex + stringLength));
                        propertiesIndex += stringLength;
                        properties.add(new Property(type, stringProperty));
                        break;

                    case MQTT_PROP_CORRELATION_DATA:
                        final short byteArrayLength = propertiesBytes.getShort(propertiesIndex);
                        propertiesIndex += Short.BYTES;
                        final byte[] byteArrayProperty = Arrays.copyOfRange(propertiesBytes.array(), propertiesIndex, propertiesIndex + byteArrayLength);
                        propertiesIndex += byteArrayLength;
                        properties.add(new Property(type, byteArrayProperty));
                        break;

                    case MQTT_PROP_USER_PROPERTY:
                        final short keyLength = propertiesBytes.getShort(propertiesIndex);
                        propertiesIndex += Short.BYTES;
                        final @NotNull String key = new String(Arrays.copyOfRange(propertiesBytes.array(), propertiesIndex, propertiesIndex + keyLength));
                        propertiesIndex += keyLength;
                        final short valueLength = propertiesBytes.getShort(propertiesIndex);
                        propertiesIndex += Short.BYTES;
                        final @NotNull byte[] value = Arrays.copyOfRange(propertiesBytes.array(), propertiesIndex, propertiesIndex + valueLength);
                        propertiesIndex += valueLength;
                        properties.add(new Property(type, key, value));
                        break;

                    default:
                        if (!forceCreationWithFailures) {
                            throw new IllegalArgumentException("Some Property/Properties could not be decoded. Exiting");
                        } else {
                            Logger.error("Some Property/Properties could not be decoded.");
                        }
                        break;
                }
            }
        }
        return Collections.unmodifiableList(properties);
    }

    /**
     * Gets the CRC value of the database File.
     *
     * @param crcBytes Byte Array containing the whole database File.
     * @param index    Index of CRC part in the Byte Array.
     * @return Returns the CRC value.
     */
    private int getCrc(final @NotNull ByteBuffer crcBytes, final int index) {
        return crcBytes.getInt(index);
    }

    /**
     * Gets the database Version value of the database File.
     *
     * @param dbVersionBytes Byte Array containing the whole database File.
     * @param index          Index of Version part in the Byte Array.
     * @return Returns the database Version value.
     */
    private int getDbVersion(final @NotNull ByteBuffer dbVersionBytes, final int index) {
        return dbVersionBytes.getInt(index);
    }

    /**
     * Reads out the ChunkType identifier part in the Byte Array.
     *
     * @param typeBytes Byte Array containing the whole database File.
     * @param index     Index of the ChunkType part in the Byte Array.
     * @return Returns an enum value of the ChunkType identifier.
     */
    private @Nullable ChunkType getChunkType(final @NotNull ByteBuffer typeBytes, final int index) {
        return ChunkType.getType(typeBytes.getInt(index));
    }

    /**
     * Reads out the length of a Chunk in the Byte Array.
     *
     * @param lengthBytes Byte Array containing the whole database File.
     * @param index       Index of the Chunk length part in the Byte Array.
     * @return Returns the Chunk's length.
     */
    private int getChunkLength(final @NotNull ByteBuffer lengthBytes, final int index) {
        return lengthBytes.getInt(index);
    }

    /**
     * Searches for retained Messages in the MessageStore List.
     * This could also be done by comparing the List of RetainedMessages with the MessageStores List.
     *
     * @return Returns an unmodifiable List of retained MessageStore-Chunks.
     */
    public @NotNull List<ChunkMsgStore> getRetainedFromMsgStore() {
        final @NotNull List<ChunkMsgStore> retainedMessages = new ArrayList<>();
        for (@NotNull ChunkMsgStore message : chunkMsgStores) {
            if (message.getRetain()) {
                retainedMessages.add(message);
            }
        }
        return Collections.unmodifiableList(retainedMessages);
    }

    /**
     * Getter for MessageStore-Chunks.
     *
     * @return Returns an unmodifiable List of MessageStore-Chunks.
     */
    public @NotNull List<ChunkMsgStore> getChunkMsgStores() {
        return Collections.unmodifiableList(chunkMsgStores);
    }

    /**
     * Getter for Client-Chunks.
     *
     * @return Returns an unmodifiable List of Client-Chunks.
     */
    public @NotNull List<ChunkClient> getChunkClients() {
        return Collections.unmodifiableList(chunkClients);
    }

    /**
     * Getter for ClientMessage-Chunks.
     *
     * @return Returns an unmodifiable List of ClientMessage-Chunks.
     */
    public @NotNull List<ChunkClientMessage> getChunkClientMessages() {
        return Collections.unmodifiableList(chunkClientMessages);
    }

    /**
     * Getter for Subscription-Chunks.
     *
     * @return Returns an unmodifiable List of Subscription-Chunks.
     */
    public @NotNull List<ChunkSubscription> getChunkSubscriptions() {
        return Collections.unmodifiableList(chunkSubscriptions);
    }
}
