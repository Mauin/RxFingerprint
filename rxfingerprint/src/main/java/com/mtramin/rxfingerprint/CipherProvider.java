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

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Enumeration;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

abstract class CipherProvider {
	static final String ANDROID_KEY_STORE = "AndroidKeyStore";
	private static final String DEFAULT_KEY_NAME = "rxfingerprint_default";

	final String keyName;
	final KeyStore keyStore;
	final boolean invalidatedByBiometricEnrollment;

	CipherProvider(@NonNull Context context, @Nullable String keyName, boolean keyInvalidatedByBiometricEnrollment) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
		if (keyName == null) {
			this.keyName = ContextUtils.getPackageName(context) + "." + DEFAULT_KEY_NAME;
		} else {
			this.keyName = keyName;
		}
		invalidatedByBiometricEnrollment = keyInvalidatedByBiometricEnrollment;
		keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
		keyStore.load(null);
	}

	/**
	 * call {@link #getCipherForEncryption()} instead
	 */
	abstract Cipher cipherForEncryption() throws GeneralSecurityException, IOException;

	@TargetApi(Build.VERSION_CODES.M)
	abstract Cipher createCipher() throws NoSuchPaddingException, NoSuchAlgorithmException;

	@NonNull
	@TargetApi(Build.VERSION_CODES.M)
	static KeyGenParameterSpec.Builder getKeyGenParameterSpecBuilder(String keyName, String blockModes, String encryptionPaddings, boolean invalidatedByBiometricEnrollment) {
		KeyGenParameterSpec.Builder builder = new KeyGenParameterSpec.Builder(keyName,
				KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
				.setBlockModes(blockModes)
				.setUserAuthenticationRequired(true)
				.setEncryptionPaddings(encryptionPaddings);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			builder.setInvalidatedByBiometricEnrollment(invalidatedByBiometricEnrollment);
		}
		return builder;
	}

	@TargetApi(Build.VERSION_CODES.M)
	Cipher getCipherForEncryption() throws IOException, GeneralSecurityException {
		try {
			return cipherForEncryption();
		} catch (KeyPermanentlyInvalidatedException e) {
			Logger.warn("Renewing invalidated key.");
			removeKey(keyName);
			return cipherForEncryption();
		}
	}

	// https://github.com/googlesamples/android-FingerprintDialog/issues/21
	// https://issuetracker.google.com/issues/65578763
	@TargetApi(Build.VERSION_CODES.M)
	Exception mapCipherFinalOperationException(Exception e) {
		boolean shouldThrowKeyPermanentlyInvalidatedException = invalidatedByBiometricEnrollment &&
				Build.VERSION.SDK_INT == 26 /*Build.VERSION_CODES.O*/ &&
				e instanceof IllegalBlockSizeException;
		if (shouldThrowKeyPermanentlyInvalidatedException) {
			Logger.warn("Removing invalidated key.");
			try {
				removeKey(keyName);
			} catch (Exception exception) {
				Logger.error("Removing invalidated key failed.", exception);
			}
			return new KeyPermanentlyInvalidatedException();

		}
		return e;
	}

	private static void removeKey(String keyName) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
		if (keyExists(keyName)) {
			KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
			keyStore.load(null);
			keyStore.deleteEntry(keyName);
		}
	}

	static boolean keyExists(String keyName) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
		KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
		keyStore.load(null);
		Enumeration<String> aliases = keyStore.aliases();

		while (aliases.hasMoreElements()) {
			if (keyName.equals(aliases.nextElement())) {
				return true;
			}
		}

		return false;
	}
}
