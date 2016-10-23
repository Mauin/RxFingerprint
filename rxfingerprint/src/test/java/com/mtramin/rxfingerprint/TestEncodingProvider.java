package com.mtramin.rxfingerprint;

public class TestEncodingProvider implements EncodingProvider {
	@Override
	public String encode(byte[] toEncode) {
		return new String(toEncode);
	}

	@Override
	public byte[] decode(String toDecode) {
		return toDecode.getBytes();
	}
}
