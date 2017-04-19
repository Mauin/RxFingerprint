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
import android.security.keystore.KeyProperties;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

class AesCipherProvider extends CipherProvider {
	private static final int AES_KEY_SIZE = 256;

	static Cipher forEncyption(@NonNull Context context, @Nullable String keyName) throws IOException, GeneralSecurityException {
		return new AesCipherProvider(context, keyName).getCipherForEncryption();
	}

	static Cipher forDecryption(@NonNull Context context, @Nullable String keyName, byte[] iv) throws CertificateException, NoSuchPaddingException, UnrecoverableKeyException, NoSuchAlgorithmException, IOException, KeyStoreException, InvalidAlgorithmParameterException, InvalidKeyException {
		return new AesCipherProvider(context, keyName).getCipherForDecryption(iv);
	}

	private AesCipherProvider(@NonNull Context context, @Nullable String keyName) throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {
		super(context, keyName);
	}

	private SecretKey findOrCreateKey(String keyName) throws NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, UnrecoverableKeyException, CertificateException, KeyStoreException, IOException {
		if (keyExists(keyName)) {
			return getKey(keyName);
		}
		return createKey(keyName);
	}

	private SecretKey getKey(String keyName) throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
		return (SecretKey) keyStore.getKey(keyName, null);
	}

	@TargetApi(Build.VERSION_CODES.M)
	private static SecretKey createKey(String keyName) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
		KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE);
		keyGenerator.init(new KeyGenParameterSpec.Builder(keyName,
				KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
				.setKeySize(AES_KEY_SIZE)
				.setBlockModes(KeyProperties.BLOCK_MODE_CBC)
				.setUserAuthenticationRequired(true)
				.setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
				.build());
		return keyGenerator.generateKey();
	}

	@Override
	Cipher cipherForEncryption() throws NoSuchAlgorithmException, NoSuchPaddingException, CertificateException, UnrecoverableKeyException, KeyStoreException, NoSuchProviderException, InvalidAlgorithmParameterException, IOException, InvalidKeyException {
		Cipher cipher = createCipher();
		SecretKey key = findOrCreateKey(keyName);
		cipher.init(Cipher.ENCRYPT_MODE, key);
		return cipher;
	}

	/**
	 * @param iv initialization vector used during encryption
	 * @return Initialized cipher for decryption operations in RxFingerprint
	 */
	Cipher getCipherForDecryption(byte[] iv) throws CertificateException, NoSuchAlgorithmException, IOException, InvalidKeyException, UnrecoverableKeyException, KeyStoreException, InvalidAlgorithmParameterException, NoSuchPaddingException {
		Cipher cipher = createCipher();
		SecretKey key = getKey(keyName);
		cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
		return cipher;
	}

	@Override
	@TargetApi(Build.VERSION_CODES.M)
	Cipher createCipher() throws NoSuchPaddingException, NoSuchAlgorithmException {
		return Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
				+ KeyProperties.BLOCK_MODE_CBC + "/"
				+ KeyProperties.ENCRYPTION_PADDING_PKCS7);
	}
}
