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

import java.util.Arrays;

/**
 * @author Lukas Brand
 * @since 1.0.0
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class Property {

    /**
     * The property identifier.
     */
    private final @NotNull PropertyType identifier;

    /**
     * The property byte value (optional).
     */
    private final byte byteValue;

    /**
     * The property short value (optional).
     */
    private final short shortValue;

    /**
     * The property integer or variable integer value. (optional)
     */
    private final int intValueOrVar;

    /**
     * The property String value. (optional)
     */
    private final @NotNull String key;

    /**
     * the property byte array value (optional)
     */
    private final @Nullable byte[] byteArrayValue;

    /**
     * Creates a Property with byte value.
     *
     * @param identifier The properties identifier.
     * @param byteValue  The properties byte value.
     */
    Property(final @NotNull PropertyType identifier, final byte byteValue) {
        this.identifier = identifier;
        this.byteValue = byteValue;

        this.byteArrayValue = null;
        this.shortValue = 0;
        this.intValueOrVar = 0;
        this.key = "";
    }

    /**
     * Creates a property with short value.
     *
     * @param identifier The properties identifier.
     * @param shortValue The properties short value.
     */
    Property(final @NotNull PropertyType identifier, final short shortValue) {
        this.identifier = identifier;
        this.shortValue = shortValue;

        this.byteArrayValue = null;
        this.byteValue = 0;
        this.intValueOrVar = 0;
        this.key = "";
    }

    /**
     * Creates a property with integer or variable integer value.
     *
     * @param identifier    The properties identifier.
     * @param intValueOrVar The properties integer or variable integer value.
     */
    Property(final @NotNull PropertyType identifier, final int intValueOrVar) {
        this.identifier = identifier;
        this.intValueOrVar = intValueOrVar;

        this.byteArrayValue = null;
        this.byteValue = 0;
        this.shortValue = 0;
        this.key = "";
    }

    /**
     * Creates a property with string value.
     *
     * @param identifier The properties identifier.
     * @param key        The properties String value.
     */
    Property(final @NotNull PropertyType identifier, final @NotNull String key) {
        this.identifier = identifier;

        this.byteArrayValue = null;
        this.byteValue = 0;
        this.shortValue = 0;
        this.intValueOrVar = 0;
        this.key = key;
    }

    /**
     * Creates a property with a byte array value.
     *
     * @param identifier     The properties identifier.
     * @param byteArrayValue The properties byte array value.
     */
    Property(final @NotNull PropertyType identifier, final @NotNull byte[] byteArrayValue) {
        this.identifier = identifier;
        this.byteArrayValue = byteArrayValue;

        this.key = "";
        this.byteValue = 0;
        this.shortValue = 0;
        this.intValueOrVar = 0;
    }

    /**
     * Creates a property with a key and its byte array value.
     *
     * @param identifier     The properties identifier.
     * @param key            The properties String key value.
     * @param byteArrayValue The properties byte array value.
     */
    Property(final @NotNull PropertyType identifier, final @NotNull String key, final @NotNull byte[] byteArrayValue) {
        this.identifier = identifier;
        this.key = key;
        this.byteArrayValue = byteArrayValue;

        this.byteValue = 0;
        this.shortValue = 0;
        this.intValueOrVar = 0;
    }

    /**
     * Getter method for the property identifier.
     *
     * @return A PropertyType which cannot be null.
     */
    public @NotNull PropertyType getIdentifier() {
        return identifier;
    }

    /**
     * Getter method for the optional property byte value.
     *
     * @return A byte.
     */
    public byte getByteValue() {
        return byteValue;
    }

    /**
     * Getter method for the optional property short value.
     *
     * @return A short.
     */
    public short getShortValue() {
        return shortValue;
    }

    /**
     * Getter method for the optional property integer or variable integer value.
     *
     * @return An integer or variable integer.
     */
    public int getIntValueOrVar() {
        return intValueOrVar;
    }

    /**
     * Getter method for the optional property String value.
     *
     * @return A String which cannot be null.
     */
    public @NotNull String getKey() {
        return key;
    }

    /**
     * Getter method for the optional property byte array value.
     *
     * @return A byte array which can be null.
     */
    public @Nullable byte[] getByteArrayValue() {
        return byteArrayValue;
    }

    /**
     * To String method for Property to print out all fields.
     *
     * @return A String which is formatted to be print out as command line information.
     */
    @Override
    public @NotNull String toString() {
        return "{" +
                identifier +
                ", byte=" + byteValue +
                ", short=" + shortValue +
                ", int=" + intValueOrVar +
                ", key='" + key + '\'' +
                ", byteArray=" + Arrays.toString(byteArrayValue) +
                '}';
    }


    //VarIntegers:

    /**
     * Reads a variable integer out of a byte array.
     *
     * @param payload byte array containing a variable integer.
     * @return A variable integer.
     */
    static int readVarInt(final @NotNull byte[] payload) {
        int index = 0;
        varIntLength = 0;
        int lword = 0;
        byte lbytes = 0;
        byte byteT;

        int remainingMult = 1;

        for (int i = 0; i < 4; i++) {
            if (index < payload.length) {
                lbytes++;
                byteT = payload[index];
                lword += (byteT & 127) * remainingMult;
                remainingMult *= 128;
                index++;
                if ((byteT & 128) == 0) {
                    if (lbytes > 1 && byteT == 0) {
                        return -1;
                    } else {
                        varIntLength = lbytes;
                        return lword;

                    }
                }
            }
        }
        return -1;
    }

    /**
     * The byte length of the read variable integer.
     */
    private static byte varIntLength = 0;

    /**
     * Getter method for the byte length of the variable integer
     *
     * @return A byte.
     */
    static byte getVarIntLength() {
        return varIntLength;
    }
}
