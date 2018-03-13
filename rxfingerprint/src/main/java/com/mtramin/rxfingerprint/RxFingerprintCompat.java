/*
 * Copyright 2018 Marvin Ramin.
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

import com.mtramin.rxfingerprint.data.FingerprintAuthenticationResult;
import com.mtramin.rxfingerprint.data.FingerprintDecryptionResult;
import com.mtramin.rxfingerprint.data.FingerprintEncryptionResult;

import io.reactivex.Observable;

class RxFingerprintCompat {

	static Observable<FingerprintAuthenticationResult> authenticate(Context context, RxFingerprintLogger logger) {
		if (FingerprintApiWrapper.isProfiteroleOrAbove()) {
			return FingerprintDialogAuthenticationObservable.create(context, logger);
		}
		return FingerprintManagerAuthenticationObservable.create(context, logger);
	}

	static Observable<FingerprintEncryptionResult> encrypt(EncryptionMethod encryptionMethod,
																  Context context,
																  String keyName,
																  char[] toEncrypt,
																  boolean keyInvalidatedByBiometricEnrollment,
																  RxFingerprintLogger logger) {
		switch (encryptionMethod) {
			case AES:
				return aesEncrypt(context, keyName, toEncrypt, keyInvalidatedByBiometricEnrollment, logger);
			case RSA:
				// RSA encryption implementation does not depend on Fingerprint authentication!
				return RsaEncryptionObservable.create(context, keyName, toEncrypt, keyInvalidatedByBiometricEnrollment, logger);
			default:
				return Observable.error(new IllegalArgumentException("Unknown encryption method: " + encryptionMethod));
		}
	}

	static Observable<FingerprintDecryptionResult> decrypt(EncryptionMethod encryptionMethod,
																  Context context,
																  String keyName,
																  String toDecrypt,
																  boolean keyInvalidatedByBiometricEnrollment,
																  RxFingerprintLogger logger) {
		switch (encryptionMethod) {
			case AES:
				return aesDecrypt(context, keyName, toDecrypt, keyInvalidatedByBiometricEnrollment, logger);
			case RSA:
				return rsaDecrypt(context, keyName, toDecrypt, keyInvalidatedByBiometricEnrollment, logger);
			default:
				return Observable.error(new IllegalArgumentException("Unknown decryption method: " + encryptionMethod));
		}
	}

	private static Observable<FingerprintEncryptionResult> aesEncrypt(Context context,
																	  String keyName,
																	  char[] toEncrypt,
																	  boolean keyInvalidatedByBiometricEnrollment,
																	  RxFingerprintLogger logger) {
		if (FingerprintApiWrapper.isProfiteroleOrAbove()) {
			return FingerprintDialogAesEncryptionObservable.create(context, keyName, toEncrypt, keyInvalidatedByBiometricEnrollment, logger);
		}
		return FingerprintManagerAesEncryptionObservable.create(context, keyName, toEncrypt, keyInvalidatedByBiometricEnrollment, logger);
	}

	private static Observable<FingerprintDecryptionResult> rsaDecrypt(Context context, String keyName, String toDecrypt, boolean keyInvalidatedByBiometricEnrollment, RxFingerprintLogger logger) {
		if (FingerprintApiWrapper.isProfiteroleOrAbove()) {
			return FingerprintDialogRsaDecryptionObservable.create(context, keyName, toDecrypt, keyInvalidatedByBiometricEnrollment, logger);
		}
		return FingerprintManagerRsaDecryptionObservable.create(context, keyName, toDecrypt, keyInvalidatedByBiometricEnrollment, logger);
	}

	private static Observable<FingerprintDecryptionResult> aesDecrypt(Context context, String keyName, String toDecrypt, boolean keyInvalidatedByBiometricEnrollment, RxFingerprintLogger logger) {
		if (FingerprintApiWrapper.isProfiteroleOrAbove()) {
			return FingerprintDialogAesDecryptionObservable.create(context, keyName, toDecrypt, keyInvalidatedByBiometricEnrollment, logger);
		}
		return FingerprintManagerAesDecryptionObservable.create(context, keyName, toDecrypt, keyInvalidatedByBiometricEnrollment, logger);
	}

}
