package com.mtramin.rxfingerprint;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;

import androidx.biometric.BiometricPrompt;
import androidx.core.hardware.fingerprint.FingerprintManagerCompat.CryptoObject;
import androidx.fragment.app.FragmentActivity;

/**
 * Created by Gbenga Oladipupo on 08/05/2020.
 */
public class BiometricHelper<T> {

    private T cryptoObject;


    public BiometricHelper(T cryptoObject1){
        this.cryptoObject = cryptoObject1;
    }

    public T getCryptoObject() {
        return  cryptoObject;
    }

    @SuppressWarnings("unchecked")
    public static <T> BiometricHelper<T> set(T cryptObj){
        return new BiometricHelper(cryptObj);
    }

}
