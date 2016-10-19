package com.mtramin.rxfingerprint;

import org.junit.Test;

public class CryptoDataTest {

	@Test
	public void fromStringValid() throws Exception {
		CryptoData.fromString("abc-_-123");
	}

	@Test(expected = CryptoDataException.class)
	public void fromInvalidString() throws Exception {
		CryptoData.fromString("123");
	}

	@Test(expected = NullPointerException.class)
	public void fromNullString() throws Exception {
		CryptoData.fromString(null);
	}
}