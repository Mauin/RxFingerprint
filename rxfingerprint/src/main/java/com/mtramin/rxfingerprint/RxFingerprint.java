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
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.mtramin.rxfingerprint.data.FingerprintAuthenticationResult;
import com.mtramin.rxfingerprint.data.FingerprintDecryptionResult;
import com.mtramin.rxfingerprint.data.FingerprintEncryptionResult;

import io.reactivex.Observable;

import static android.Manifest.permission.USE_FINGERPRINT;

/**
 * Entry point for RxFingerprint. Contains all the base methods you need to interact with the
 * fingerprint sensor of the device. Allows authentication of the user via the fingerprint
 * sensor of his/her device.
 * <p/>
 * To just authenticate the user with his fingerprint, use {@link #authenticate(Context)}.
 * <p/>
 * To encrypt given data and authenticate the user with his fingerprint,
 * call {@link #encrypt(Context, String)}
 * <p/>
 * To decrypt previously encrypted data via the {@link #encrypt(Context, String)} method,
 * call {@link #decrypt(Context, String)}
 * <p/>
 * The {@link #encrypt(Context, String, String)} and {@link #decrypt(Context, String, String)}
 * methods use the custom keyName provided as an argument to store the encryption keys in the
 * Android {@link java.security.KeyStore}. {@link #encrypt(Context, String)} and
 * {@link #decrypt(Context, String)} will generate a default key name which contains the
 * applications package name.
 * <p/>
 * Helper methods provide information about the devices capability to handle fingerprint
 * authentication. For fingerprint authentication to be isAvailable, the device needs to contain the
 * necessary hardware (a sensor) and the user has to have enrolled at least one fingerprint.
 */
public class RxFingerprint {
    /**
     * Authenticate the user with his fingerprint. This will enable the fingerprint sensor on the
     * device and wait for the user to touch the sensor with his finger.
     * <p/>
     * All possible recoverable errors will be provided in {@link org.reactivestreams.Subscriber#onNext(Object)} and
     * should be handled there. Unrecoverable errors will be provided with
     * {@link org.reactivestreams.Subscriber#onError(Throwable)} calls.
     *
     * @param context current context
     * @return Observable {@link FingerprintAuthenticationResult}. Will complete once the
     * authentication was successful or has failed entirely.
     */
    public static Observable<FingerprintAuthenticationResult> authenticate(Context context) {
        return FingerprintAuthenticationObservable.create(context);
    }

    /**
     * Encrypt data and authenticate the user with his fingerprint. The encrypted data can only be
     * accessed again by calling {@link #decrypt(Context, String)}. Will use a default keyName in
     * the Android keystore unique to this applications package name.
     * If you want to provide a custom key name use {@link #encrypt(Context, String, String)}
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
     * @param context   context to use
     * @param toEncrypt data to encrypt
     * @return Observable {@link FingerprintEncryptionResult} that will contain the encrypted data.
     * Will complete once the authentication and encryption were successful or have failed entirely.
     */
    public static Observable<FingerprintEncryptionResult> encrypt(Context context, String toEncrypt) {
        return FingerprintEncryptionObservable.create(context, toEncrypt);
    }

    /**
     * Decrypt data previously encrypted with {@link #encrypt(Context, String)}.
     * <p/>
     * The encrypted string should be exactly the one you previously received as a result of the
     * {@link #encrypt(Context, String)} method.
     * <p/>
     * The resulting {@link FingerprintDecryptionResult} will contain the decrypted string as a
     * String and is accessible via {@link FingerprintDecryptionResult#getDecrypted()} if the
     * authentication and decryption was successful.
     *
     * @param context   context to use.
     * @param encrypted String of encrypted data previously encrypted with
     *                  {@link #encrypt(Context, String)}.  @return Observable
     *                  {@link FingerprintDecryptionResult} that will contain the decrypted data.
     *                  Will complete once the authentication and decryption were
     *                  successful or have failed entirely.
     * @return Observable result of the decryption operation. Will contain the
     * decrypted string if decryption was successful.
     */
    public static Observable<FingerprintDecryptionResult> decrypt(Context context, String encrypted) {
        return FingerprintDecryptionObservable.create(context, encrypted);
    }

    /**
     * Encrypt data  and authenticate the user with his fingerprint. The encrypted data can only be
     * accessed again by calling {@link #decrypt(Context, String, String)} with the same keyName.
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
     * @param context   context to use
     * @param keyName   name of the key in the keystore to use
     * @param toEncrypt data to encrypt
     * @return Observable {@link FingerprintEncryptionResult} that will contain the encrypted data.
     * Will complete once the authentication and encryption were successful or have failed entirely.
     */
    public static Observable<FingerprintEncryptionResult> encrypt(Context context, @NonNull String keyName, @NonNull String toEncrypt) {
        return FingerprintEncryptionObservable.create(context, keyName, toEncrypt);
    }

    /**
     * Decrypt data previously encrypted with {@link #encrypt(Context, String, String)}.
     * To decrypt, you have to provide the same keyName that you used for encryption.
     * <p/>
     * The encrypted string should be exactly the one you previously received as a result of the
     * {@link #encrypt(Context, String, String)} method.
     * <p/>
     * The resulting {@link FingerprintDecryptionResult} will contain the decrypted string as a
     * String and is accessible via {@link FingerprintDecryptionResult#getDecrypted()} if the
     * authentication and decryption was successful.
     *
     * @param context   context to use.
     * @param keyName   name of the key in the keystore to use
     * @param encrypted String of encrypted data previously encrypted with
     *                  {@link #encrypt(Context, String)}.  @return Observable
     *                  {@link FingerprintDecryptionResult} that will contain the decrypted data.
     *                  Will complete once the authentication and decryption were
     *                  successful or have failed entirely.
     * @return Observable result of the decryption
     */
    public static Observable<FingerprintDecryptionResult> decrypt(Context context, @NonNull String keyName, @NonNull String encrypted) {
        return FingerprintDecryptionObservable.create(context, keyName, encrypted);
    }

    /**
     * Provides information if fingerprint authentication is currently available.
     * <p/>
     * The device needs to have a fingerprint hardware and the user needs to have enrolled
     * at least one fingerprint in the system.
     *
     * @param context a context
     * @return {@code true} if fingerprint authentication is isAvailable
     */
    public static boolean isAvailable(@NonNull Context context) {
        return isHardwareDetected(context) && hasEnrolledFingerprints(context);
    }

    /**
     * Provides information if fingerprint authentication is unavailable.
     * <p/>
     * The device needs to have a fingerprint hardware and the user needs to have enrolled
     * at least one fingerprint in the system.
     *
     * @param context a context
     * @return {@code true} if fingerprint authentication is unavailable
     */
    public static boolean isUnavailable(@NonNull Context context) {
        return !isAvailable(context);
    }

    /**
     * Provides information if the device contains fingerprint detection hardware.
     * <p/>
     * If you want to detect if fingerprint authentication is currently available, prefer
     * {@link RxFingerprint#isAvailable(Context)}.
     *
     * @param context a context
     * @return {@code true} if fingerprint hardware exists in this device.
     */
    @SuppressWarnings("MissingPermission")
    public static boolean isHardwareDetected(@NonNull Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return false;
        }

        FingerprintManager fingerprintManager = getFingerprintManager(context);
        if (fingerprintManager == null) {
            return false;
        }

        return fingerprintPermissionGranted(context) && fingerprintManager.isHardwareDetected();
    }

    /**
     * Provides information if the user has enrolled at least one fingerprint.
     * <p/>
     * If you want to detect if fingerprint authentication is currently available, prefer
     * {@link RxFingerprint#isAvailable(Context)}.
     *
     * @param context a context
     * @return {@code true} if at least one fingerprint was enrolled.
     */
    @SuppressWarnings("MissingPermission")
    public static boolean hasEnrolledFingerprints(@NonNull Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return false;
        }

        FingerprintManager fingerprintManager = getFingerprintManager(context);
        if (fingerprintManager == null) {
            return false;
        }
        return fingerprintPermissionGranted(context) && fingerprintManager.hasEnrolledFingerprints();
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private static boolean fingerprintPermissionGranted(Context context) {
        return context.checkSelfPermission(USE_FINGERPRINT) == PackageManager.PERMISSION_GRANTED;
    }

    @Nullable
    @RequiresApi(Build.VERSION_CODES.M)
    static FingerprintManager getFingerprintManager(Context context) {
        try {
            return (FingerprintManager) context.getSystemService(Context.FINGERPRINT_SERVICE);
        } catch (Exception | NoClassDefFoundError e) {
            Log.e("RxFingerprint", "Device with SDK >=23 doesn't provide Fingerprint APIs", e);
        }
        return null;
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
     * @param throwable Throwable received in {@link org.reactivestreams.Subscriber#onError(Throwable)} from
     *                  an {@link RxFingerprint} encryption method
     * @return {@code true} if the requested key was permanently invalidated and cannot be used
     * anymore
     */
    public static boolean keyInvalidated(Throwable throwable) {
        return throwable instanceof KeyPermanentlyInvalidatedException;
    }
}
