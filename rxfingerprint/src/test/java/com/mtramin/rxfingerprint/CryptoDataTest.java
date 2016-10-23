package com.mtramin.rxfingerprint;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertTrue;

public class CryptoDataTest {

	private static final String TEST_MESSAGE = "testMessage";
	private static final String TEST_IV = "testIv";
	private static final String INPUT = TEST_MESSAGE + CryptoData.SEPARATOR + TEST_IV;

	private EncodingProvider encodingProvider = new TestEncodingProvider();

	@Test
	public void fromString() throws Exception {
		CryptoData cryptoData = CryptoData.fromString(encodingProvider, INPUT);

		assertTrue(Arrays.equals(TEST_MESSAGE.getBytes(), cryptoData.getMessage()));
		assertTrue(Arrays.equals(TEST_IV.getBytes(), cryptoData.getIv()));
		assertTrue(cryptoData.toString().contains(CryptoData.SEPARATOR));
	}

	@Test
	public void fromBytes() throws Exception {
		CryptoData cryptoData = CryptoData.fromBytes(encodingProvider, TEST_MESSAGE.getBytes(), TEST_IV.getBytes());

		assertTrue(Arrays.equals(TEST_MESSAGE.getBytes(), cryptoData.getMessage()));
		assertTrue(Arrays.equals(TEST_IV.getBytes(), cryptoData.getIv()));
		assertTrue(cryptoData.toString().contains(CryptoData.SEPARATOR));
	}

	@Test(expected = CryptoDataException.class)
	public void verifyInvalidString() throws Exception {
		CryptoData.verifyCryptoDataString("123");
	}

	@Test(expected = CryptoDataException.class)
	public void verifyEmptyString() throws Exception {
		CryptoData.verifyCryptoDataString("");
	}

	@Test(expected = NullPointerException.class)
	public void verifyNullString() throws Exception {
		CryptoData.verifyCryptoDataString(null);
	}
}