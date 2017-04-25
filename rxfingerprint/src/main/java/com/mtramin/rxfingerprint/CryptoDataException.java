/*
 * Copyright 2017 Marvin Ramin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * <http://www.apache.org/licenses/LICENSE-2.0>
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
