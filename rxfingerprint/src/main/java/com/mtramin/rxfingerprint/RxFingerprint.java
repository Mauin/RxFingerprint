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
 * TODO: JAVADOC
 */
public class RxFingerprint {

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

    public static Observable<FingerprintAuthenticationResult> authenticate(Context context) {
        return FingerprintAuthenticationObservable.create(context);
    }

    public static Observable<FingerprintEncryptionResult> encrypt(Context context, String toEncrypt) {
        return FingerprintEncryptionObservable.create(context, toEncrypt);
    }

    public static Observable<FingerprintDecryptionResult> decrypt(Context context, String encrypted) {
        return FingerprintDecryptionObservable.create(context, encrypted);
    }
}
