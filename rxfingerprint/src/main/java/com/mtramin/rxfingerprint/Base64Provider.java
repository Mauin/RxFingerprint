package com.mtramin.rxfingerprint;

import android.util.Base64;

/**
 * EncodingProvider that encodes and decodes from/to Base64
 */
class Base64Provider implements EncodingProvider {

	@Override
	public String encode(byte[] toEncode) {
		return Base64.encodeToString(toEncode, Base64.DEFAULT);
	}

	@Override
	public byte[] decode(String toDecode) {
		return Base64.decode(toDecode, Base64.DEFAULT);
	}
}
