/*
 * Copyright 2015 Marvin Ramin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mtramin.rxfingerprint;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;

import com.mtramin.rxfingerprint.data.FingerprintEncryptionResult;
import com.mtramin.rxfingerprint.data.FingerprintResult;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidParameterSpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;

import rx.Observable;
import rx.Subscriber;

/**
 * Encrypts data with fingerprint authentication. Initializes a {@link Cipher} for encryption which
 * can only be used with fingerprint authentication and uses it once authentication was successful
 * to encrypt the given data.
 */
class FingerprintEncryptionObservable extends FingerprintObservable<FingerprintEncryptionResult> {

	private final String keyName;
	private final String toEncrypt;
	private final EncodingProvider encodingProvider;

	/**
	 * Creates a new FingerprintEncryptionObservable that will listen to fingerprint authentication
	 * to encrypt the given data.
	 *
	 * @param context   context to use
	 * @param keyName   name of the key in the keystore
	 * @param toEncrypt data to encrypt  @return Observable {@link FingerprintEncryptionResult}
	 */
	static Observable<FingerprintEncryptionResult> create(Context context, String keyName, String toEncrypt) {
		return Observable.create(new FingerprintEncryptionObservable(context, keyName, toEncrypt, new Base64Provider()));
	}

	/**
	 * Creates a new FingerprintEncryptionObservable that will listen to fingerprint authentication
	 * to encrypt the given data.
	 *
	 * @param context   context to use
	 * @param toEncrypt data to encrypt  @return Observable {@link FingerprintEncryptionResult}
	 */
	static Observable<FingerprintEncryptionResult> create(Context context, String toEncrypt) {
		return Observable.create(new FingerprintEncryptionObservable(context, null, toEncrypt, new Base64Provider()));
	}

	private FingerprintEncryptionObservable(Context context, String keyName, String toEncrypt, EncodingProvider encodingProvider) {
		super(context);
		this.keyName = keyName;

		if (toEncrypt == null) {
			throw new NullPointerException("String to be encrypted is null. Can only encrypt valid strings");
		}
		this.toEncrypt = toEncrypt;
		this.encodingProvider = encodingProvider;
	}

	@Nullable
	@Override
	protected FingerprintManagerCompat.CryptoObject initCryptoObject(Subscriber<FingerprintEncryptionResult> subscriber) {
		CryptoProvider cryptoProvider = new CryptoProvider(context, keyName);
		try {
			Cipher cipher = cryptoProvider.initEncryptionCipher();
			return new FingerprintManagerCompat.CryptoObject(cipher);
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchProviderException | NoSuchPaddingException | InvalidAlgorithmParameterException | CertificateException | UnrecoverableKeyException | KeyStoreException | IOException e) {
			subscriber.onError(e);
			return null;
		}

	}

	@Override
	protected void onAuthenticationSucceeded(Subscriber<FingerprintEncryptionResult> subscriber, FingerprintManagerCompat.AuthenticationResult result) {
		try {
			Cipher cipher = result.getCryptoObject().getCipher();
			byte[] encryptedBytes = cipher.doFinal(toEncrypt.getBytes("UTF-8"));
			byte[] ivBytes = cipher.getParameters().getParameterSpec(IvParameterSpec.class).getIV();

			String encryptedString = CryptoData.fromBytes(encodingProvider, encryptedBytes, ivBytes).toString();
			CryptoData.verifyCryptoDataString(encryptedString);

			subscriber.onNext(new FingerprintEncryptionResult(FingerprintResult.AUTHENTICATED, null, encryptedString));
			subscriber.onCompleted();
		} catch (CryptoDataException | IllegalBlockSizeException | BadPaddingException | InvalidParameterSpecException | UnsupportedEncodingException e) {
			subscriber.onError(e);
		}
	}

	@Override
	protected void onAuthenticationHelp(Subscriber<FingerprintEncryptionResult> subscriber, int helpMessageId, String helpString) {
		subscriber.onNext(new FingerprintEncryptionResult(FingerprintResult.HELP, helpString, null));
	}

	@Override
	protected void onAuthenticationFailed(Subscriber<FingerprintEncryptionResult> subscriber) {
		subscriber.onNext(new FingerprintEncryptionResult(FingerprintResult.FAILED, null, null));
	}
}
