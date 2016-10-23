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
