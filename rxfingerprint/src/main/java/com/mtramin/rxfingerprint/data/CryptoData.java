package com.mtramin.rxfingerprint.data;

import android.support.annotation.NonNull;
import android.util.Base64;

/**
 * Data of a cryptographic operation with RxFingerprint.
 */
public class CryptoData {
    private static final String SEPARATOR = "///";

    private final String messageEncoded;
    private final String ivEncoded;

    private CryptoData(byte[] messageBytes, byte[] ivBytes) {
        this.messageEncoded = encode(messageBytes);
        this.ivEncoded = encode(ivBytes);
    }

    private CryptoData(String message, String iv) {
        this.messageEncoded = message;
        this.ivEncoded = iv;
    }

    /**
     * Sets up data from an input string.
     *
     * @param input input string that was previously encrypted by RxFingerprint
     * @return parsed data
     */
    public static CryptoData fromString(String input) {
        String[] inputParams = input.split(SEPARATOR);
        return new CryptoData(inputParams[0], inputParams[1]);
    }

    /**
     * Sets up data from encrypted byte that resulted from encryption operation.
     *
     * @param messageBytes encrypted bytes of message
     * @param ivBytes      initialization vector in bytes
     * @return parsed data
     */
    public static CryptoData fromBytes(byte[] messageBytes, byte[] ivBytes) {
        return new CryptoData(messageBytes, ivBytes);
    }

    @Override
    public String toString() {
        return this.messageEncoded + SEPARATOR + this.ivEncoded;
    }

    /**
     * @return initialization vector of the crypto operation
     */
    public byte[] getIv() {
        return decode(ivEncoded);
    }

    /**
     * @return message of the crypto operation
     */
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
