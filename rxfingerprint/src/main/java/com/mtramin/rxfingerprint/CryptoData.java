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

/**
 * Data of a cryptographic operation with RxFingerprint.
 */
class CryptoData {
	static final String SEPARATOR = "-_-";

	private final EncodingProvider encodingProvider;

    private final String messageEncoded;
    private final String ivEncoded;

    private CryptoData(EncodingProvider encodingProvider, byte[] messageBytes, byte[] ivBytes) {
		this.encodingProvider = encodingProvider;
		messageEncoded = encodingProvider.encode(messageBytes);
		ivEncoded = encodingProvider.encode(ivBytes);
    }

    private CryptoData(EncodingProvider encodingProvider, String message, String iv) {
		this.encodingProvider = encodingProvider;
		messageEncoded = message;
		ivEncoded = iv;
    }

    /**
	 * Sets up data from an input string.
	 *
	 * @param input input string that was previously encrypted by RxFingerprint
	 * @return parsed data
	 */
	static CryptoData fromString(EncodingProvider encodingProvider, String input) throws CryptoDataException {
		verifyCryptoDataString(input);

		String[] inputParams = input.split(SEPARATOR);
		return new CryptoData(encodingProvider, inputParams[0], inputParams[1]);
	}

	/**
	 * Sets up data from encrypted byte that resulted from encryption operation.
	 *
	 * @param messageBytes encrypted bytes of message
	 * @param ivBytes      initialization vector in bytes
	 * @return parsed data
	 */
	static CryptoData fromBytes(EncodingProvider encodingProviders, byte[] messageBytes, byte[] ivBytes) {
		return new CryptoData(encodingProviders, messageBytes, ivBytes);
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

    @Override
    public String toString() {
        return messageEncoded + SEPARATOR + ivEncoded;
    }

    /**
     * @return initialization vector of the crypto operation
     */
     byte[] getIv() {
        return encodingProvider.decode(ivEncoded);
    }

    /**
     * @return message of the crypto operation
     */
     byte[] getMessage() {
        return encodingProvider.decode(messageEncoded);
    }
}
