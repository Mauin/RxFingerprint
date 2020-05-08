package com.mtramin.rxfingerprint;

import android.os.Build;

import androidx.biometric.BiometricPrompt;
import androidx.core.hardware.fingerprint.FingerprintManagerCompat;

/**
 * Created by Gbenga Oladipupo on 08/05/2020.
 */
public class AuthenticationCallbackHelper<T> {

    private T authenticationCallback;

    private AuthenticationCallbackHelper(T authenticationCallback){
        this.authenticationCallback = authenticationCallback;
    }


    @SuppressWarnings("unchecked")
    protected static <T>  AuthenticationCallbackHelper<T> set(T authCallback){

        return new AuthenticationCallbackHelper(authCallback);
    }


    public T getAuthenticationCallback() {
        return authenticationCallback;
    }
}
