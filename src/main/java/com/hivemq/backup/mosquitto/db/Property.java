package com.hivemq.backup.mosquitto.db;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

/**
 * @author Lukas Brand
 */
public class Property {

    private final @NotNull PropertyType identifier;
    private final byte byteValue;
    private final short shortValue;
    private final int intValueOrVar;
    private final @NotNull String key;
    private final @Nullable byte[] byteArrayValue;


    Property(final @NotNull PropertyType identifier, final byte byteValue) {
        this.identifier = identifier;
        this.byteValue = byteValue;

        this.byteArrayValue = null;
        this.shortValue = 0;
        this.intValueOrVar = 0;
        this.key = "";
    }

    @SuppressWarnings("unused")
    Property(final @NotNull PropertyType identifier, final short shortValue) {
        this.identifier = identifier;
        this.shortValue = shortValue;

        this.byteArrayValue = null;
        this.byteValue = 0;
        this.intValueOrVar = 0;
        this.key = "";
    }

    Property(final @NotNull PropertyType identifier, final int intValueOrVar) {
        this.identifier = identifier;
        this.intValueOrVar = intValueOrVar;

        this.byteArrayValue = null;
        this.byteValue = 0;
        this.shortValue = 0;
        this.key = "";
    }

    Property(final @NotNull PropertyType identifier, final @NotNull String key) {
        this.identifier = identifier;

        this.byteArrayValue = null;
        this.byteValue = 0;
        this.shortValue = 0;
        this.intValueOrVar = 0;
        this.key = key;
    }

    Property(final @NotNull PropertyType identifier, final @NotNull byte[] byteArrayValue) {
        this.identifier = identifier;
        this.byteArrayValue = byteArrayValue;

        this.key = "";
        this.byteValue = 0;
        this.shortValue = 0;
        this.intValueOrVar = 0;
    }

    Property(final @NotNull PropertyType identifier, final @NotNull String key, final @NotNull byte[] byteArrayValue) {
        this.identifier = identifier;
        this.key = key;
        this.byteArrayValue = byteArrayValue;

        this.byteValue = 0;
        this.shortValue = 0;
        this.intValueOrVar = 0;
    }

    public @NotNull PropertyType getIdentifier() {
        return identifier;
    }

    public byte getByteValue() {
        return byteValue;
    }

    public short getShortValue() {
        return shortValue;
    }

    public int getIntValueOrVar() {
        return intValueOrVar;
    }

    public @NotNull String getKey() {
        return key;
    }

    public @Nullable byte[] getByteArrayValue() {
        return byteArrayValue;
    }


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

    public static int readVarInt(final @NotNull byte[] payload) {
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

    private static byte varIntLength = 0;

    public static byte getVarIntLength() {
        return varIntLength;
    }
}
