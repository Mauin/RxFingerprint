/*
 * Copyright 2015 Marvin Ramin
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
 */

package com.mtramin.rxfingerprint.data;

import android.support.annotation.NonNull;
import android.util.Base64;

/**
 * Data of a cryptographic operation with RxFingerprint.
 */
public class CryptoData {
    private static final String SEPARATOR = "-_-";

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
        if (input == null) {
            throw new NullPointerException("Input for decryption is null. Make sure to provide a valid, encrypted String for decryption.");
        }

        if (input.isEmpty() || !input.contains(SEPARATOR)) {
            throw new IllegalArgumentException("Invalid input given for decryption operation. Make sure you provide a string that was previously encrypted by RxFingerprint.");
        }

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
