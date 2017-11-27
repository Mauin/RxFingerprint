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
import android.security.keystore.KeyProperties;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.spec.KeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

class RsaCipherProvider extends CipherProvider {
	RsaCipherProvider(@NonNull Context context, @Nullable String keyName) throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {
		this(context, keyName, true);
	}

	RsaCipherProvider(@NonNull Context context, @Nullable String keyName, boolean keyInvalidatedByBiometricEnrollment) throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {
		super(context, keyName, keyInvalidatedByBiometricEnrollment);
	}
	@Override
	@TargetApi(Build.VERSION_CODES.M)
	Cipher cipherForEncryption() throws GeneralSecurityException, IOException {
		KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, ANDROID_KEY_STORE);

		keyGenerator.initialize(getKeyGenParameterSpecBuilder(keyName, KeyProperties.BLOCK_MODE_ECB, KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1, invalidatedByBiometricEnrollment)
				.build());

		keyGenerator.generateKeyPair();

		KeyFactory keyFactory = KeyFactory.getInstance(KeyProperties.KEY_ALGORITHM_RSA);
		Cipher cipher = createCipher();
		cipher.init(Cipher.ENCRYPT_MODE, getPublicKey(keyFactory, keyStore));

		return cipher;
	}

	Cipher getCipherForDecryption() throws GeneralSecurityException {
		Cipher cipher = createCipher();
		cipher.init(Cipher.DECRYPT_MODE, getPrivateKey(keyStore, keyName));
		return cipher;
	}

	private PrivateKey getPrivateKey(KeyStore keyStore, String keyAlias) throws GeneralSecurityException {
		return (PrivateKey) keyStore.getKey(keyAlias, null);
	}

	private PublicKey getPublicKey(KeyFactory keyFactory, KeyStore keyStore) throws GeneralSecurityException {
		PublicKey publicKey = keyStore.getCertificate(keyName).getPublicKey();
		KeySpec spec = new X509EncodedKeySpec(publicKey.getEncoded());
		return keyFactory.generatePublic(spec);
	}

	@Override
	@TargetApi(Build.VERSION_CODES.M)
	Cipher createCipher() throws NoSuchPaddingException, NoSuchAlgorithmException {
		return Cipher.getInstance(KeyProperties.KEY_ALGORITHM_RSA + "/"
				+ KeyProperties.BLOCK_MODE_ECB + "/"
				+ KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1);
	}
}
