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

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.fingerprint.FingerprintManager.AuthenticationResult;
import android.hardware.fingerprint.FingerprintManager.CryptoObject;
import android.support.annotation.Nullable;

import com.mtramin.rxfingerprint.data.FingerprintDecryptionResult;
import com.mtramin.rxfingerprint.data.FingerprintEncryptionResult;
import com.mtramin.rxfingerprint.data.FingerprintResult;

import javax.crypto.Cipher;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;

/**
 * Decrypts data with fingerprint authentication. Initializes a {@link Cipher} for decryption which
 * can only be used with fingerprint authentication and uses it once authentication was successful
 * to encrypt the given data.
 * <p/>
 * The date handed in must be previously encrypted by a {@link AesEncryptionObservable}.
 */
@SuppressLint("NewApi") // SDK check happens in {@link FingerprintObservable#subscribe}
class AesDecryptionObservable extends FingerprintObservable<FingerprintDecryptionResult> {

	private final AesCipherProvider cipherProvider;
	private final String encryptedString;
	private final EncodingProvider encodingProvider;

	/**
	 * Creates a new AesEncryptionObservable that will listen to fingerprint authentication
	 * to encrypt the given data.
	 *
	 * @param context   context to use
	 * @param keyName   keyName to use for the decryption
	 * @param encrypted data to encrypt  @return Observable {@link FingerprintEncryptionResult}
	 * @return Observable result of the decryption
	 */
	static Observable<FingerprintDecryptionResult> create(Context context, String keyName, String encrypted) {
		try {
			return Observable.create(new AesDecryptionObservable(new FingerprintApiWrapper(context),
					new AesCipherProvider(context, keyName),
					encrypted,
					new Base64Provider()));
		} catch (Exception e) {
			return Observable.error(e);
		}
	}

	private AesDecryptionObservable(FingerprintApiWrapper fingerprintApiWrapper,
									AesCipherProvider cipherProvider,
									String encrypted,
									EncodingProvider encodingProvider) {
		super(fingerprintApiWrapper);
		this.cipherProvider = cipherProvider;
		encryptedString = encrypted;
		this.encodingProvider = encodingProvider;
	}

	@Nullable
	@Override
	protected CryptoObject initCryptoObject(ObservableEmitter<FingerprintDecryptionResult> subscriber) {
		try {
			CryptoData cryptoData = CryptoData.fromString(encodingProvider, encryptedString);
			Cipher cipher = cipherProvider.getCipherForDecryption(cryptoData.getIv());
			return new CryptoObject(cipher);
		} catch (Exception e) {
			subscriber.onError(e);
			return null;
		}
	}

	@Override
	protected void onAuthenticationSucceeded(ObservableEmitter<FingerprintDecryptionResult> emitter, AuthenticationResult result) {
		try {
			CryptoData cryptoData = CryptoData.fromString(encodingProvider, encryptedString);
			Cipher cipher = result.getCryptoObject().getCipher();
			byte[] bytes = cipher.doFinal(cryptoData.getMessage());

			emitter.onNext(new FingerprintDecryptionResult(FingerprintResult.AUTHENTICATED, null, ConversionUtils.toChars(bytes)));
			emitter.onComplete();
		} catch (Exception e) {
			emitter.onError(cipherProvider.mapCipherFinalOperationException(e));
		}

	}

	@Override
	protected void onAuthenticationHelp(ObservableEmitter<FingerprintDecryptionResult> emitter, int helpMessageId, String helpString) {
		emitter.onNext(new FingerprintDecryptionResult(FingerprintResult.HELP, helpString, null));
	}

	@Override
	protected void onAuthenticationFailed(ObservableEmitter<FingerprintDecryptionResult> emitter) {
		emitter.onNext(new FingerprintDecryptionResult(FingerprintResult.FAILED, null, null));
	}
}
