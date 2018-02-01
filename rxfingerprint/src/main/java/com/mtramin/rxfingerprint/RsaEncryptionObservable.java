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

import android.content.Context;
import android.support.annotation.VisibleForTesting;

import com.mtramin.rxfingerprint.data.FingerprintEncryptionResult;
import com.mtramin.rxfingerprint.data.FingerprintResult;
import com.mtramin.rxfingerprint.data.FingerprintUnavailableException;

import javax.crypto.Cipher;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

class RsaEncryptionObservable implements ObservableOnSubscribe<FingerprintEncryptionResult> {

	private final FingerprintApiWrapper fingerprintApiWrapper;
	private final RsaCipherProvider cipherProvider;
	private final char[] toEncrypt;
	private final EncodingProvider encodingProvider;

	/**
	 * Creates a new AesEncryptionObservable that will listen to fingerprint authentication
	 * to encrypt the given data.
	 *
	 * @param context   context to use
	 * @param keyName   name of the key in the keystore
	 * @param toEncrypt data to encrypt  @return Observable {@link FingerprintEncryptionResult}
	 */
	static Observable<FingerprintEncryptionResult> create(Context context, String keyName, char[] toEncrypt, boolean keyInvalidatedByBiometricEnrollment) {
		if (toEncrypt == null) {
			return Observable.error(new IllegalArgumentException("String to be encrypted is null. Can only encrypt valid strings"));
		}
		try {
			return Observable.create(new RsaEncryptionObservable(new FingerprintApiWrapper(context),
					new RsaCipherProvider(context, keyName, keyInvalidatedByBiometricEnrollment),
					toEncrypt,
					new Base64Provider()));
		} catch (Exception e) {
			return Observable.error(e);
		}
	}

	@VisibleForTesting
	RsaEncryptionObservable(FingerprintApiWrapper fingerprintApiWrapper,
							RsaCipherProvider cipherProvider,
							char[] toEncrypt,
							EncodingProvider encodingProvider) {
		this.fingerprintApiWrapper = fingerprintApiWrapper;
		this.cipherProvider = cipherProvider;
		this.toEncrypt = toEncrypt;
		this.encodingProvider = encodingProvider;
	}

	@Override
	public void subscribe(ObservableEmitter<FingerprintEncryptionResult> emitter) throws Exception {
		if (fingerprintApiWrapper.isUnavailable()) {
			emitter.onError(new FingerprintUnavailableException("Fingerprint authentication is not available on this device! Ensure that the device has a Fingerprint sensor and enrolled Fingerprints by calling RxFingerprint#isAvailable(Context) first"));
			return;
		}

		try {
			Cipher cipher = cipherProvider.getCipherForEncryption();
			byte[] encryptedBytes = cipher.doFinal(ConversionUtils.toBytes(toEncrypt));

			String encryptedString = encodingProvider.encode(encryptedBytes);
			emitter.onNext(new FingerprintEncryptionResult(FingerprintResult.AUTHENTICATED, null, encryptedString));
			emitter.onComplete();
		} catch (Exception e) {
			Logger.error(String.format("Error writing value for key: %s", cipherProvider.keyName), e);
			emitter.onError(e);
		}
	}
}
