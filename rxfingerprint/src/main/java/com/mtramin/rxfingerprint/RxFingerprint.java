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
import android.content.Intent;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;

import com.mtramin.rxfingerprint.data.FingerprintAuthenticationResult;
import com.mtramin.rxfingerprint.data.FingerprintDecryptionResult;
import com.mtramin.rxfingerprint.data.FingerprintEncryptionResult;

import org.reactivestreams.Subscriber;

import io.reactivex.Observable;

import static android.provider.Settings.ACTION_FINGERPRINT_ENROLL;

/**
 * Entry point for RxFingerprint. Contains all the base methods you need to interact with the
 * fingerprint sensor of the device. Allows authentication of the user via the fingerprint
 * sensor of his/her device.
 * <p/>
 * To just authenticate the user with his fingerprint, use {@link #authenticate()}.
 * <p/>
 * To encrypt given data and authenticate the user with his fingerprint,
 * call {@link #encrypt(String, char[])}
 * <p/>
 * To decrypt previously encrypted data via the {@link #encrypt(String, char[])}
 * method, call {@link #decrypt(String, String)}
 * <p/>
 * Helper methods provide information about the devices capability to handle fingerprint
 * authentication. For fingerprint authentication to be isAvailable, the device needs to contain the
 * necessary hardware (a sensor) and the user has to have enrolled at least one fingerprint.
 */
public class RxFingerprint {

	private final Context context;
	private final boolean keyInvalidatedByBiometricEnrollment;
	private final EncryptionMethod encryptionMethod;
	private final RxFingerprintLogger logger;
	private final FingerprintDialogBundle fingerprintDialogBundle;

	private RxFingerprint(Context context,
						  boolean keyInvalidatedByBiometricEnrollment,
						  EncryptionMethod encryptionMethod,
						  RxFingerprintLogger logger,
						  FingerprintDialogBundle fingerprintDialogBundle) {
		this.context = context;
		this.keyInvalidatedByBiometricEnrollment = keyInvalidatedByBiometricEnrollment;
		this.encryptionMethod = encryptionMethod;
		this.logger = logger;
		this.fingerprintDialogBundle = fingerprintDialogBundle;
	}

	/**
	 * Builder for {@link RxFingerprint}
	 */
	public static class Builder {
		private final Context context;
		private boolean keyInvalidatedByBiometricEnrollment = true;
		private EncryptionMethod encryptionMethod = EncryptionMethod.RSA;
		private RxFingerprintLogger logger = new DefaultLogger();
		@NonNull private String dialogTitleText;
		@Nullable private String dialogSubtitleText;
		@Nullable private String dialogDescriptionText;
		@NonNull private String dialogNegativeButtonText;

		/**
		 * Creates a new Builder for {@link RxFingerprint}
		 *
		 * @param context context to use for this instance of RxFingerprint. While an application
		 *                {@link Context} will work, it might cause issues with configuration
		 *                changes of the application. Prefer an {@link android.app.Activity}.
		 */
		public Builder(@NonNull Context context) {
			this.context = context;
		}

		public Builder dialogTitleText(String dialogTitleText) {
			this.dialogTitleText = dialogTitleText;
			return this;
		}

		public Builder dialogSubtitleText(String dialogSubtitleText) {
			this.dialogSubtitleText = dialogSubtitleText;
			return this;
		}

		public Builder dialogDescriptionText(String dialogDescriptionText) {
			this.dialogDescriptionText = dialogDescriptionText;
			return this;
		}

		public Builder dialogNegativeButtonText(String dialogNegativeButtonText) {
			this.dialogNegativeButtonText = dialogNegativeButtonText;
			return this;
		}

		/**
		 * Changes behavior of encryption keys when a user changes their biometric enrollments.
		 * e.g. fingerprints were added/removed from the system settings of the users device.
		 * By default the encryption keys will be invalidated and encrypted data will be
		 * inaccessible after changes to biometric enrollments have been made. By setting this to
		 * {@code false} this behavior can be overridden and keys will stay valid even when changes
		 * to biometric enrollments have been made on the users device.
		 *
		 * @param shouldInvalidate define whether keys should be invalidated when changes to the
		 *                         devices biometric enrollments have been made. Defaults to
		 *                         {@code true}.
		 * @return the {@link Builder}
		 */
		@NonNull
		public Builder keyInvalidatedByBiometricEnrollment(boolean shouldInvalidate) {
			this.keyInvalidatedByBiometricEnrollment = shouldInvalidate;
			return this;
		}

		/**
		 * Sets the {@link EncryptionMethod} that will be used for this instance of
		 * {@link RxFingerprint}. AES requires user authentication for both
		 * encryption/decryption. RSA requires user authentication only for
		 * decryption. For more details see {@link EncryptionMethod}.
		 *
		 * @param encryptionMethod the encryption method to be used for all encryption/decryption
		 *                         operations of this RxFingerprint instance. Defaults to
		 *                         @{link EncryptionMethod.RSA}.
		 * @return the {@link Builder}
		 */
		@NonNull
		public Builder encryptionMethod(@NonNull EncryptionMethod encryptionMethod) {
			this.encryptionMethod = encryptionMethod;
			return this;
		}

		/**
		 * Sets the logging implementation to be used.
		 *
		 * @param logger Logger implementation to be used. Use {@link Builder#disableLogging} to
		 *               disable all logging from RxFingerprint.
		 *               Defaults to {@link DefaultLogger} which
		 *               logs to logcat via {@link android.util.Log}.
		 * @return the {@link Builder}
		 */
		@NonNull
		public Builder logger(@NonNull RxFingerprintLogger logger) {
			this.logger = logger;
			return this;
		}

		/**
		 * Disables all logging for RxFingerprint
		 *
		 * @return the {@link Builder}
		 */
		@NonNull
		public Builder disableLogging() {
			this.logger = new EmptyLogger();
			return this;
		}

		public RxFingerprint build() {
			if (dialogTitleText == null) {
				throw new IllegalArgumentException("RxFingerprint requires a dialotTitleText.");
			}

			if (dialogNegativeButtonText == null) {
				throw new IllegalArgumentException("RxFingerprint requires a dialogNegativeButtonText.");
			}

			return new RxFingerprint(context,
					keyInvalidatedByBiometricEnrollment,
					encryptionMethod,
					logger,
					new FingerprintDialogBundle(
							dialogTitleText,
							dialogSubtitleText,
							dialogDescriptionText,
							dialogNegativeButtonText)
			);
		}
	}

	/**
     * Authenticate the user with his fingerprint. This will enable the fingerprint sensor on the
     * device and wait for the user to touch the sensor with his finger.
     * <p/>
     * All possible recoverable errors will be provided in {@link Subscriber#onNext(Object)} and
     * should be handled there. Unrecoverable errors will be provided with
     * {@link Subscriber#onError(Throwable)} calls.
     *
     * @return Observable {@link FingerprintAuthenticationResult}. Will complete once the
     * authentication was successful or has failed entirely.
     */
    public Observable<FingerprintAuthenticationResult> authenticate() {
        return RxFingerprintCompat.authenticate(context, fingerprintDialogBundle, logger);
    }

	/**
	 * Encrypt data and authenticate the user with his fingerprint. The encrypted data can only be
	 * accessed again by calling {@link #decrypt(String)}. Will use a default keyName in
	 * the Android keystore unique to this applications package name.
	 * If you want to provide a custom key name use {@link #encrypt(String, String)}
	 * instead.
	 * <p/>
	 * Encrypted data is only accessible after the user has authenticated with
	 * fingerprint authentication.
	 * <p/>
	 * Encryption uses AES encryption with CBC blocksize and PKCS7 padding.
	 * The key-length for AES encryption is set to 265 bits by default.
	 * <p/>
	 * The resulting {@link FingerprintEncryptionResult} will contain the encrypted data as a String
	 * and is accessible via {@link FingerprintEncryptionResult#getEncrypted()} if the
	 * authentication was successful. Save this data where you please, but don't change it if you
	 * want to decrypt it again!
	 *
	 * @param toEncrypt data to encrypt
	 * @return Observable {@link FingerprintEncryptionResult} that will contain the encrypted data.
	 * Will complete once the authentication and encryption were successful or have failed entirely.
	 */
	public Observable<FingerprintEncryptionResult> encrypt(@NonNull String toEncrypt) {
		return encrypt(toEncrypt.toCharArray());
	}

	/**
	 * Encrypt data and authenticate the user with his fingerprint. The encrypted data can only be
	 * accessed again by calling {@link #decrypt(String, String)}. Will use a default keyName in
	 * the Android keystore unique to this applications package name.
	 * <p/>
	 * Encrypted data is only accessible after the user has authenticated with
	 * fingerprint authentication.
	 * <p/>
	 * Encryption uses AES encryption with CBC blocksize and PKCS7 padding.
	 * The key-length for AES encryption is set to 265 bits by default.
	 * <p/>
	 * The resulting {@link FingerprintEncryptionResult} will contain the encrypted data as a String
	 * and is accessible via {@link FingerprintEncryptionResult#getEncrypted()} if the
	 * authentication was successful. Save this data where you please, but don't change it if you
	 * want to decrypt it again!
	 *
	 * @param toEncrypt data to encrypt
	 * @return Observable {@link FingerprintEncryptionResult} that will contain the encrypted data.
	 * Will complete once the authentication and encryption were successful or have failed entirely.
	 */
	public Observable<FingerprintEncryptionResult> encrypt(@Nullable String keyName, @NonNull String toEncrypt) {
		return encrypt(keyName, toEncrypt.toCharArray());
	}

	/**
	 * Encrypt data and authenticate the user with his fingerprint. The encrypted data can only be
	 * accessed again by calling {@link #decrypt(String)}. Will use a default keyName in
	 * the Android keystore unique to this applications package name.
	 * If you want to provide a custom key name use {@link #encrypt(String, char[])}
	 * instead.
	 * <p/>
	 * Encrypted data is only accessible after the user has authenticated with
	 * fingerprint authentication.
	 * <p/>
	 * Encryption uses AES encryption with CBC blocksize and PKCS7 padding.
	 * The key-length for AES encryption is set to 265 bits by default.
	 * <p/>
	 * The resulting {@link FingerprintEncryptionResult} will contain the encrypted data as a String
	 * and is accessible via {@link FingerprintEncryptionResult#getEncrypted()} if the
	 * authentication was successful. Save this data where you please, but don't change it if you
	 * want to decrypt it again!
	 *
	 *  @param toEncrypt data to encrypt
	 * @return Observable {@link FingerprintEncryptionResult} that will contain the encrypted data.
	 * Will complete once the authentication and encryption were successful or have failed entirely.
	 */
	public Observable<FingerprintEncryptionResult> encrypt(@NonNull char[] toEncrypt) {
		return encrypt(null, toEncrypt);
	}

	/**
	 * Encrypt data with the given {@link EncryptionMethod}. Depending on the given method, the
	 * fingerprint sensor might be enabled and waiting for the user to authenticate before the
	 * encryption step. All encrypted data can only be accessed again by calling
	 * {@link #decrypt(String, String)} with the same
	 * {@link EncryptionMethod} that was used for encryption of the given value.
	 * <p>
	 * Take more details about the encryption method and how they behave from {@link EncryptionMethod}
	 * <p>
	 * The resulting {@link FingerprintEncryptionResult} will contain the encrypted data as a String
	 * and is accessible via {@link FingerprintEncryptionResult#getEncrypted()} if the
	 * operation was successful. Save this data where you please, but don't change it if you
	 * want to decrypt it again!
	 *
	 * @param keyName   name of the key to store in the Android {@link java.security.KeyStore}
	 * @param toEncrypt data to encrypt
	 * @return Observable {@link FingerprintEncryptionResult} that will contain the encrypted data.
	 * Will complete once the operation was successful or failed entirely.
	 */
	public Observable<FingerprintEncryptionResult> encrypt(@Nullable String keyName,
														   @NonNull char[] toEncrypt) {
		return RxFingerprintCompat.encrypt(encryptionMethod, context, fingerprintDialogBundle, keyName, toEncrypt, keyInvalidatedByBiometricEnrollment, logger);
	}

	/**
	 * Decrypt data previously encrypted with {@link #encrypt(String)}.
	 * <p/>
	 * The encrypted string should be exactly the one you previously received as a result of the
	 * {@link #encrypt(String)} method.
	 * <p/>
	 * The resulting {@link FingerprintDecryptionResult} will contain the decrypted string as a
	 * String and is accessible via {@link FingerprintDecryptionResult#getDecrypted()} if the
	 * authentication and decryption was successful.
	 *
	 * @param encrypted String of encrypted data previously encrypted with
	 *                  {@link #encrypt(String, char[])}.
	 * @return Observable {@link FingerprintDecryptionResult} that will contain the decrypted data.
	 * Will complete once the authentication and decryption were
	 * successful or have failed entirely.
	 * decrypted string if decryption was successful.
	 */
	public Observable<FingerprintDecryptionResult> decrypt(@NonNull String encrypted) {
		return decrypt(null, encrypted);
	}

	/**
	 * Decrypt data previously encrypted with {@link #encrypt(String, char[])}.
	 * Make sure the {@link EncryptionMethod} matches to one that was used for encryption of this value.
	 * To decrypt, you have to provide the same keyName that you used for encryption.
	 * <p/>
	 * The encrypted string should be exactly the one you previously received as a result of the
	 * {@link #encrypt(String, char[])} method.
	 * <p/>
	 * The resulting {@link FingerprintDecryptionResult} will contain the decrypted string as a
	 * String and is accessible via {@link FingerprintDecryptionResult#getDecrypted()} if the
	 * authentication and decryption was successful.
	 * <p>
	 * This operation will require the user to authenticate with their fingerprint.
	 *
	 * @param keyName   name of the key in the keystore to use
	 * @param toDecrypt String of encrypted data previously encrypted with
	 *                  {@link #encrypt(String, char[])}.
	 * @return Observable  {@link FingerprintDecryptionResult} that will contain the decrypted data.
	 *                  Will complete once the authentication and decryption were successful or
	 *                  have failed entirely.
	 */
	public Observable<FingerprintDecryptionResult> decrypt(@Nullable String keyName,
														   @NonNull String toDecrypt) {
		return RxFingerprintCompat.decrypt(encryptionMethod, context, fingerprintDialogBundle, keyName, toDecrypt, keyInvalidatedByBiometricEnrollment, logger);
	}

    /**
     * Provides information if fingerprint authentication is currently available.
     * <p/>
     * The device needs to have a fingerprint hardware and the user needs to have enrolled
     * at least one fingerprint in the system.
     *
     * @return {@code true} if fingerprint authentication is isAvailable
     */
    public boolean isAvailable() {
        return new FingerprintApiWrapper(context, logger).isAvailable();
    }

    /**
     * Provides information if fingerprint authentication is unavailable.
     * <p/>
     * The device needs to have a fingerprint hardware and the user needs to have enrolled
     * at least one fingerprint in the system.
     *
     * @return {@code true} if fingerprint authentication is unavailable
     */
    public boolean isUnavailable() {
        return !isAvailable();
    }

    /**
     * Provides information if the device contains fingerprint detection hardware.
     * <p/>
     * If you want to detect if fingerprint authentication is currently available, prefer
     * {@link RxFingerprint#isAvailable()}.
     *
     * @return {@code true} if fingerprint hardware exists in this device.
	 * @deprecated Scheduled to be removed in v3.1. Use {@link RxFingerprint#isAvailable()} instead.
     */
    @SuppressWarnings("MissingPermission")
	@Deprecated
    public boolean isHardwareDetected() {
        return new FingerprintApiWrapper(context, logger).isHardwareDetected();
    }

    /**
     * Provides information if the user has enrolled at least one fingerprint.
     * <p/>
     * If you want to detect if fingerprint authentication is currently available, prefer
     * {@link RxFingerprint#isAvailable()}.
     *
     * @return {@code true} if at least one fingerprint was enrolled.
	 * @deprecated Scheduled to be removed in v3.1. Use {@link RxFingerprint#isAvailable()} instead.
     */
    @SuppressWarnings("MissingPermission")
	@Deprecated
    public boolean hasEnrolledFingerprints() {
        return new FingerprintApiWrapper(context, logger).hasEnrolledFingerprints();
    }

    public void launchFingerprintEnrollment() {
		context.startActivity(new Intent(ACTION_FINGERPRINT_ENROLL));
	}

    /**
     * Checks if the provided {@link Throwable} is of type {@link KeyPermanentlyInvalidatedException}
     * <p/>
     * This would mean that the user has disabled the lock screen on his device or changed the
     * fingerprints stored on the device for authentication.
     * <p/>
     * If the user does this all keys encrypted by {@link RxFingerprint} become permanently
     * invalidated by the Android system. To continue using encryption you have to ask the user to
     * encrypt the original data again. The old data is not accessible anymore.
     *
     * @param throwable Throwable received in {@link Subscriber#onError(Throwable)} from
     *                  an {@link RxFingerprint} encryption method
     * @return {@code true} if the requested key was permanently invalidated and cannot be used
     * anymore
     */
	@RequiresApi(23)
    public static boolean keyInvalidated(Throwable throwable) {
        return throwable instanceof KeyPermanentlyInvalidatedException;
    }
}
