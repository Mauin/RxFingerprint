package com.mtramin.rxfingerprint.data;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class FingerprintEncryptionResultTest {

	@Test
	public void getResultSuccess() throws Exception {
		String decrypted = "decrypted";
		FingerprintEncryptionResult result = FingerprintEncryptionResult.create(FingerprintResult.AUTHENTICATED, null, decrypted);

		assertNull(result.getMessage());
		assertEquals(decrypted, result.getEncrypted());
	}

	@Test(expected = IllegalAccessError.class)
	public void getResultFailure() throws Exception {
		String message = "some error happened";
		FingerprintEncryptionResult result = FingerprintEncryptionResult.create(FingerprintResult.FAILED, message, null);

		assertEquals(message, result.getMessage());
		result.getEncrypted();
	}

	@Test(expected = IllegalAccessError.class)
	public void getResultHelp() throws Exception {
		String message = "help";
		FingerprintEncryptionResult result = FingerprintEncryptionResult.create(FingerprintResult.HELP, message, null);

		assertEquals(message, result.getMessage());
		result.getEncrypted();
	}

}