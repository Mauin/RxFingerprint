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

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import com.mtramin.rxfingerprint.data.FingerprintEncryptionResult;
import com.mtramin.rxfingerprint.data.FingerprintResult;
import com.mtramin.rxfingerprint.data.FingerprintUnavailableException;

import javax.crypto.Cipher;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

@SuppressLint("NewApi") // SDK check happens in {@link FingerprintObservable#subscribe}
class RsaEncryptionObservable implements ObservableOnSubscribe<FingerprintEncryptionResult> {

	private final String keyName;
	private final String toEncrypt;
	private final EncodingProvider encodingProvider;
	private final Context context;

	/**
	 * Creates a new AesEncryptionObservable that will listen to fingerprint authentication
	 * to encrypt the given data.
	 *
	 * @param context   context to use
	 * @param keyName   name of the key in the keystore
	 * @param toEncrypt data to encrypt  @return Observable {@link FingerprintEncryptionResult}
	 */
	static Observable<FingerprintEncryptionResult> create(Context context, String keyName, String toEncrypt) {
		return Observable.create(new RsaEncryptionObservable(context, keyName, toEncrypt));
	}

	private RsaEncryptionObservable(Context context, String keyName, String toEncrypt) {
		if (toEncrypt == null) {
			throw new NullPointerException("String to be encrypted is null. Can only encrypt valid strings");
		}
		this.toEncrypt = toEncrypt;
		this.keyName = keyName;
		this.context = context;
		this.encodingProvider = new Base64Provider();
	}

	@Override
	public void subscribe(ObservableEmitter<FingerprintEncryptionResult> emitter) throws Exception {
		if (RxFingerprint.isUnavailable(context)) {
			emitter.onError(new FingerprintUnavailableException("Fingerprint authentication is not available on this device! Ensure that the device has a Fingerprint sensor and enrolled Fingerprints by calling RxFingerprint#isAvailable(Context) first"));
			return;
		}

		try {
			Cipher cipher = RsaCipherProvider.forEncryption(context, keyName);
			byte[] encryptedBytes = cipher.doFinal(toEncrypt.getBytes("UTF-8"));

			String encryptedString = encodingProvider.encode(encryptedBytes);
			emitter.onNext(new FingerprintEncryptionResult(FingerprintResult.AUTHENTICATED, null, encryptedString));
			emitter.onComplete();
		} catch (Exception e) {
			Log.w("RxFingerprint", String.format("Error writing value for key: %s", keyName), e);
			emitter.onError(e);
		}
	}
}