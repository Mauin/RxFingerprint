package com.mtramin.rxfingerprint.data;

import android.support.annotation.NonNull;
import android.util.Base64;

/**
 * TODO: JAVADOC
 */
public class CryptoData {
    private static final String SEPARATOR = "///";

    private String messageEncoded;
    private String ivEncoded;

    private CryptoData(byte[] messageBytes, byte[] ivBytes) {
        this.messageEncoded = encode(messageBytes);
        this.ivEncoded = encode(ivBytes);
    }

    private CryptoData(String message, String iv) {
        this.messageEncoded = message;
        this.ivEncoded = iv;
    }

    public static CryptoData fromString(String input) {
        String[] inputParams = input.split(SEPARATOR);
        return new CryptoData(inputParams[0], inputParams[1]);
    }

    public static CryptoData fromBytes(byte[] messageBytes, byte[] ivBytes) {
        return new CryptoData(messageBytes, ivBytes);
    }

    @Override
    public String toString() {
        return this.messageEncoded + SEPARATOR + this.ivEncoded;
    }

    public byte[] getIv() {
        return decode(ivEncoded);
    }

    public byte[] getMessage() {
        return decode(messageEncoded);
    }

    private byte[] decode(String messageEncoded) {
        return Base64.decode(messageEncoded, Base64.DEFAULT);
    }

    @NonNull
    private String encode(byte[] messageBytes) {
        return Base64.encodeToString(messageBytes, Base64.DEFAULT);
    }
}
