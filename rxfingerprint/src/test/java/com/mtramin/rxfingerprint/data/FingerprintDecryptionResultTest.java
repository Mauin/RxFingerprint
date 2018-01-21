package com.mtramin.rxfingerprint.data;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class FingerprintDecryptionResultTest {

	@Test
	public void getResultSuccess() throws Exception {
		char[] decrypted = "decrypted".toCharArray();
		FingerprintDecryptionResult result = new FingerprintDecryptionResult(FingerprintResult.AUTHENTICATED, null, decrypted);

		assertNull(result.getMessage());
		assertEquals(decrypted, result.getDecryptedChars());
	}

	@Test(expected = IllegalAccessError.class)
	public void getResultFailure() throws Exception {
		String message = "some error happened";
		FingerprintDecryptionResult result = new FingerprintDecryptionResult(FingerprintResult.FAILED, message, null);

		assertEquals(message, result.getMessage());
		result.getDecrypted();
	}

	@Test(expected = IllegalAccessError.class)
	public void getResultHelp() throws Exception {
		String message = "help";
		FingerprintDecryptionResult result = new FingerprintDecryptionResult(FingerprintResult.HELP, message, null);

		assertEquals(message, result.getMessage());
		result.getDecrypted();
	}
}