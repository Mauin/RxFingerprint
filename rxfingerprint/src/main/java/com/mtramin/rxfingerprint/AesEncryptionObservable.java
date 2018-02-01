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

import com.mtramin.rxfingerprint.data.FingerprintEncryptionResult;
import com.mtramin.rxfingerprint.data.FingerprintResult;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;

/**
 * Encrypts data with fingerprint authentication. Initializes a {@link Cipher} for encryption which
 * can only be used with fingerprint authentication and uses it once authentication was successful
 * to encrypt the given data.
 */
@SuppressLint("NewApi") // SDK check happens in {@link FingerprintObservable#subscribe}
class AesEncryptionObservable extends FingerprintObservable<FingerprintEncryptionResult> {

	private final char[] toEncrypt;
	private final EncodingProvider encodingProvider;
	private final AesCipherProvider cipherProvider;

	/**
	 * Creates a new AesEncryptionObservable that will listen to fingerprint authentication
	 * to encrypt the given data.
	 *
	 * @param context   context to use
	 * @param keyName   name of the key in the keystore
	 * @param toEncrypt data to encrypt  @return Observable {@link FingerprintEncryptionResult}
	 */
	static Observable<FingerprintEncryptionResult> create(Context context, String keyName, char[] toEncrypt, boolean keyInvalidatedByBiometricEnrollment) {
		try {
			return Observable.create(new AesEncryptionObservable(new FingerprintApiWrapper(context),
					new AesCipherProvider(context, keyName, keyInvalidatedByBiometricEnrollment),
					toEncrypt,
					new Base64Provider()));
		} catch (Exception e) {
			return Observable.error(e);
		}
	}

	private AesEncryptionObservable(FingerprintApiWrapper fingerprintApiWrapper,
							AesCipherProvider cipherProvider,
							char[] toEncrypt,
							EncodingProvider encodingProvider) {
		super(fingerprintApiWrapper);
		this.cipherProvider = cipherProvider;

		if (toEncrypt == null) {
			throw new NullPointerException("String to be encrypted is null. Can only encrypt valid strings");
		}
		this.toEncrypt = toEncrypt;
		this.encodingProvider = encodingProvider;
	}

	@Nullable
	@Override
	protected CryptoObject initCryptoObject(ObservableEmitter<FingerprintEncryptionResult> emitter) {
		try {
			Cipher cipher = cipherProvider.getCipherForEncryption();
			return new CryptoObject(cipher);
		} catch (Exception e) {
			emitter.onError(e);
			return null;
		}
	}

	@Override
	protected void onAuthenticationSucceeded(ObservableEmitter<FingerprintEncryptionResult> emitter, AuthenticationResult result) {
		try {
			Cipher cipher = result.getCryptoObject().getCipher();
			byte[] encryptedBytes = cipher.doFinal(ConversionUtils.toBytes(toEncrypt));
			byte[] ivBytes = cipher.getParameters().getParameterSpec(IvParameterSpec.class).getIV();

			String encryptedString = CryptoData.fromBytes(encodingProvider, encryptedBytes, ivBytes).toString();
			CryptoData.verifyCryptoDataString(encryptedString);

			emitter.onNext(new FingerprintEncryptionResult(FingerprintResult.AUTHENTICATED, null, encryptedString));
			emitter.onComplete();
		} catch (Exception e) {
			emitter.onError(cipherProvider.mapCipherFinalOperationException(e));
		}
	}

	@Override
	protected void onAuthenticationHelp(ObservableEmitter<FingerprintEncryptionResult> emitter, int helpMessageId, String helpString) {
		emitter.onNext(new FingerprintEncryptionResult(FingerprintResult.HELP, helpString, null));
	}

	@Override
	protected void onAuthenticationFailed(ObservableEmitter<FingerprintEncryptionResult> emitter) {
		emitter.onNext(new FingerprintEncryptionResult(FingerprintResult.FAILED, null, null));
	}
}
