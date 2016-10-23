package com.mtramin.rxfingerprint;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CryptoDataExceptionTest {

	@Test
	public void fromInputEmpty() throws Exception {
		CryptoDataException cryptoDataException = CryptoDataException.fromCryptoDataString("");

		String expected = String.format(CryptoDataException.ERROR_MSG, true, false);
		assertEquals(expected, cryptoDataException.getMessage());
	}

	@Test
	public void fromInvalidFormat() throws Exception {
		CryptoDataException cryptoDataException = CryptoDataException.fromCryptoDataString("abc");

		String expected = String.format(CryptoDataException.ERROR_MSG, false, false);
		assertEquals(expected, cryptoDataException.getMessage());
	}

	@Test
	public void fromCorrectFormat() throws Exception {
		CryptoDataException cryptoDataException = CryptoDataException.fromCryptoDataString("abc-_-123");

		String expected = String.format(CryptoDataException.ERROR_MSG, false, true);
		assertEquals(expected, cryptoDataException.getMessage());
	}
}