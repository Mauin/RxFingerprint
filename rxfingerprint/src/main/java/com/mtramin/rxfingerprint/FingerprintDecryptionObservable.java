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
import android.support.annotation.VisibleForTesting;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;

import com.mtramin.rxfingerprint.data.FingerprintDecryptionResult;
import com.mtramin.rxfingerprint.data.FingerprintEncryptionResult;
import com.mtramin.rxfingerprint.data.FingerprintResult;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import rx.Observable;
import rx.Subscriber;

/**
 * Decrypts data with fingerprint authentication. Initializes a {@link Cipher} for decryption which
 * can only be used with fingerprint authentication and uses it once authentication was successful
 * to encrypt the given data.
 * <p/>
 * The date handed in must be previously encrypted by a {@link FingerprintEncryptionObservable}.
 */
class FingerprintDecryptionObservable extends FingerprintObservable<FingerprintDecryptionResult> {

	private final String keyName;
	private final String encryptedString;
	private final EncodingProvider encodingProvider;

	/**
	 * Creates a new FingerprintEncryptionObservable that will listen to fingerprint authentication
	 * to encrypt the given data.
	 *
	 * @param context   context to use
	 * @param keyName   keyName to use for the decryption
	 * @param encrypted data to encrypt  @return Observable {@link FingerprintEncryptionResult}
	 * @return Observable result of the decryption
	 */
	static Observable<FingerprintDecryptionResult> create(Context context, String keyName, String encrypted) {
		return Observable.create(new FingerprintDecryptionObservable(context, keyName, encrypted, new Base64Provider()));
	}

	/**
	 * Creates a new FingerprintEncryptionObservable that will listen to fingerprint authentication
	 * to encrypt the given data.
	 *
	 * @param context   context to use
	 * @param encrypted data to encrypt  @return Observable {@link FingerprintEncryptionResult}
	 * @return Observable result of the decryption
	 */
	static Observable<FingerprintDecryptionResult> create(Context context, String encrypted) {
		return Observable.create(new FingerprintDecryptionObservable(context, null, encrypted, new Base64Provider()));
	}

	@VisibleForTesting
	FingerprintDecryptionObservable(Context context, String keyName, String encrypted, EncodingProvider encodingProvider) {
		super(context);
		this.keyName = keyName;
		encryptedString = encrypted;
		this.encodingProvider = encodingProvider;
	}

	@Nullable
	@Override
	protected FingerprintManagerCompat.CryptoObject initCryptoObject(Subscriber<FingerprintDecryptionResult> subscriber) {
		CryptoProvider cryptoProvider = new CryptoProvider(context, keyName);
		try {
			CryptoData cryptoData = CryptoData.fromString(encodingProvider, encryptedString);
			Cipher cipher = cryptoProvider.initDecryptionCipher(cryptoData.getIv());
			return new FingerprintManagerCompat.CryptoObject(cipher);
		} catch (CryptoDataException | NoSuchAlgorithmException | CertificateException | InvalidKeyException | KeyStoreException | InvalidAlgorithmParameterException | NoSuchPaddingException | IOException | UnrecoverableKeyException e) {
			subscriber.onError(e);
			return null;
		}
	}

	@Override
	protected void onAuthenticationSucceeded(Subscriber<FingerprintDecryptionResult> subscriber, FingerprintManagerCompat.AuthenticationResult result) {
		try {
			CryptoData cryptoData = CryptoData.fromString(encodingProvider, encryptedString);
			Cipher cipher = result.getCryptoObject().getCipher();
			String decrypted = new String(cipher.doFinal(cryptoData.getMessage()));

			subscriber.onNext(new FingerprintDecryptionResult(FingerprintResult.AUTHENTICATED, null, decrypted));
			subscriber.onCompleted();
		} catch (CryptoDataException | BadPaddingException | IllegalBlockSizeException e) {
			subscriber.onError(e);
		}

	}

	@Override
	protected void onAuthenticationHelp(Subscriber<FingerprintDecryptionResult> subscriber, int helpMessageId, String helpString) {
		subscriber.onNext(new FingerprintDecryptionResult(FingerprintResult.HELP, helpString, null));
	}

	@Override
	protected void onAuthenticationFailed(Subscriber<FingerprintDecryptionResult> subscriber) {
		subscriber.onNext(new FingerprintDecryptionResult(FingerprintResult.FAILED, null, null));
	}
}
