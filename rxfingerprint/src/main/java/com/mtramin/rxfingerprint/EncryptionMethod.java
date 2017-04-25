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

/**
 * Methods to be used for encryption/decryption.
 * <p>
 * These differ in the algorithm they use and also the behavior of the fingerprint sensor during
 * encryption.
 * <p>
 * Decryption will in all cases require the user to authenticate with their fingerprint.
 * <p>
 * <b>Make sure to always use the same method when encrypting and decrypting a given key.</b>
 */
public enum EncryptionMethod {

	/**
	 * Uses the Asymmetric Encryption Standard (AES) for encryption and decryption. A 256-bit key
	 * will be used for the operation.
	 * <p>
	 * Using AES will require fingerprint authentication for both encryption and decryption.
	 */
	AES,

	/**
	 * Uses the RSA public-key encryption standard.
	 * <p>
	 * Using RSA will only require fingerprint authentication for decryption. Values can be
	 * encrypted without the user needing to authenticate their fingerprint.
	 */
	RSA
}
