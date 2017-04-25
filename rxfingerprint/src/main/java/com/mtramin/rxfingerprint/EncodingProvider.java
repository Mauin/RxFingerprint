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
 * Defines a provider for encoding and decoding
 */
interface EncodingProvider {

	/**
	 * Encodes the given byte array to a String
	 * @param toEncode bytes to encode
	 * @return encoded string
	 */
	String encode(byte[] toEncode);

	/**
	 * Decodes the given string to a byte array.
	 * @param toDecode string to decode
	 * @return decoded bytes
	 */
	byte[] decode(String toDecode);
}
