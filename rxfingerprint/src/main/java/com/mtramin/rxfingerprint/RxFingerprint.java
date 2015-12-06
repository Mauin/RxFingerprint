package com.mtramin.rxfingerprint;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;

import com.mtramin.rxfingerprint.data.FingerprintAuthenticationResult;
import com.mtramin.rxfingerprint.data.FingerprintDecryptionResult;
import com.mtramin.rxfingerprint.data.FingerprintEncryptionResult;
import com.mtramin.rxfingerprint.observables.FingerprintAuthenticationObservable;
import com.mtramin.rxfingerprint.observables.FingerprintDecryptionObservable;
import com.mtramin.rxfingerprint.observables.FingerprintEncryptionObservable;

import rx.Observable;

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
 * Helper methods provide information about the devices capability to handle fingerprint
 * authentication. For fingerprint authentication to be available, the device needs to contain the
 * necessary hardware (a sensor) and the user has to have enrolled at least one fingerprint.
 */
public class RxFingerprint {

    /**
     * Authenticate the user with his fingerprint.
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
     * accessed again by calling {@link #decrypt(Context, String)}. Encrypted data is only
     * accessible after the user has authenticated with fingerprint authentication.
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
     *
     * The encrypted string should be exactly the one you previously received as a result of the
     * {@link #encrypt(Context, String)} method.
     *
     * The resulting {@link FingerprintDecryptionResult} will contain the decrypted string as a
     * String and is accessible via {@link FingerprintDecryptionResult#getDecrypted()} if the
     * authentication and decryption was successful.
     *
     * @param context   context to use.
     * @param encrypted String of encrypted data previously encrypted with
     *                  {@link #encrypt(Context, String)}.
     * @return Observable {@link FingerprintDecryptionResult} that will contain the decrypted data.
     * Will complete once the authentication and decryption were successful or have failed entirely.
     */
    public static Observable<FingerprintDecryptionResult> decrypt(Context context, String encrypted) {
        return FingerprintDecryptionObservable.create(context, encrypted);
    }

    /**
     * Provides information if fingerprint authentication is currently available.
     * <p/>
     * The device needs to have a fingerprint hardware and the user needs to have enrolled
     * at least one fingerprint in the system.
     *
     * @param context a context
     * @return {@code true} if fingerprint authentication is available
     */
    public static boolean available(@NonNull Context context) {
        return isHardwareDetected(context) && hasEnrolledFingerprints(context);
    }

    /**
     * Provides information if the device contains fingerprint detection hardware.
     * <p/>
     * If you want to detect if fingerprint authentication is currently available, prefer
     * {@link RxFingerprint#available(Context)}.
     *
     * @param context a context
     * @return {@code true} if fingerprint hardware exists in this device.
     */
    public static boolean isHardwareDetected(@NonNull Context context) {
        return getFingerprintManager(context).isHardwareDetected();
    }

    /**
     * Provides information if the user has enrolled at least one fingerprint.
     * <p/>
     * If you want to detect if fingerprint authentication is currently available, prefer
     * {@link RxFingerprint#available(Context)}.
     *
     * @param context a context
     * @return {@code true} if at least one fingerprint was enrolled.
     */
    public static boolean hasEnrolledFingerprints(@NonNull Context context) {
        return getFingerprintManager(context).hasEnrolledFingerprints();
    }

    @NonNull
    private static FingerprintManagerCompat getFingerprintManager(Context context) {
        return FingerprintManagerCompat.from(context);
    }
}
