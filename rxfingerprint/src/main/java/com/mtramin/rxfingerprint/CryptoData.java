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

package com.mtramin.rxfingerprint;

import android.support.annotation.NonNull;
import android.util.Base64;

/**
 * Data of a cryptographic operation with RxFingerprint.
 */
class CryptoData {
	static final String SEPARATOR = "-_-";

    private final String messageEncoded;
    private final String ivEncoded;

    private CryptoData(byte[] messageBytes, byte[] ivBytes) {
		messageEncoded = encode(messageBytes);
		ivEncoded = encode(ivBytes);
    }

    private CryptoData(String message, String iv) {
		messageEncoded = message;
		ivEncoded = iv;
    }

    /**
	 * Sets up data from an input string.
	 *
	 * @param input input string that was previously encrypted by RxFingerprint
	 * @return parsed data
	 */
	static CryptoData fromString(String input) throws CryptoDataException {
		verifyCryptoDataString(input);

		String[] inputParams = input.split(SEPARATOR);
		return new CryptoData(inputParams[0], inputParams[1]);
	}

	/**
	 * Checks if the given input is a valid encrypted string. Will throw an exception if the input
	 * is invalid.
	 *
	 * @param input input to verify
	 */
	static void verifyCryptoDataString(String input) throws CryptoDataException {
		if (input.isEmpty() || !input.contains(SEPARATOR)) {
			throw CryptoDataException.fromCryptoDataString(input);
		}
	}

	/**
	 * Sets up data from encrypted byte that resulted from encryption operation.
     *
     * @param messageBytes encrypted bytes of message
     * @param ivBytes      initialization vector in bytes
     * @return parsed data
     */
    static CryptoData fromBytes(byte[] messageBytes, byte[] ivBytes) {
        return new CryptoData(messageBytes, ivBytes);
    }

    @Override
    public String toString() {
        return messageEncoded + SEPARATOR + ivEncoded;
    }

    /**
     * @return initialization vector of the crypto operation
     */
     byte[] getIv() {
        return decode(ivEncoded);
    }

    /**
     * @return message of the crypto operation
     */
     byte[] getMessage() {
        return decode(messageEncoded);
    }

     private static byte[] decode(String messageEncoded) {
        return Base64.decode(messageEncoded, Base64.DEFAULT);
    }

    @NonNull
    private static String encode(byte[] messageBytes) {
        return Base64.encodeToString(messageBytes, Base64.DEFAULT);
    }
}
