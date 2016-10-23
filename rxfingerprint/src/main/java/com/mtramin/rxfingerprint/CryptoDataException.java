package com.mtramin.rxfingerprint;

import static com.mtramin.rxfingerprint.CryptoData.SEPARATOR;

/**
 * Exception thrown when CryptoData is invalid
 */
class CryptoDataException extends Exception {

	static final String ERROR_MSG = "Invalid input given for decryption operation. Make sure you provide a string that was previously encrypted by RxFingerprint. empty: %s, correct format: %s";

	private CryptoDataException(String message) {
		super(message);
	}

	static CryptoDataException fromCryptoDataString(String input) {
		boolean isEmpty = input.isEmpty();
		boolean containsSeparator = input.contains(SEPARATOR);
		String message = String.format(ERROR_MSG, isEmpty, containsSeparator);

		return new CryptoDataException(message);
	}
}
