package com.mtramin.rxfingerprint;

import android.content.Context;
import android.hardware.biometrics.BiometricManager;
import android.util.Log;

import com.mtramin.rxfingerprint.data.FingerprintAuthenticationException;

import java.lang.ref.WeakReference;
import java.security.Signature;

import javax.crypto.Cipher;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricPrompt;
import androidx.core.hardware.fingerprint.FingerprintManagerCompat;
import androidx.fragment.app.FragmentActivity;
import io.reactivex.ObservableEmitter;

/**
 * Created by Gbenga Oladipupo on 08/05/2020.
 */
public class BiometricPromptProvider {

    private static final String TAG ="BiometricPromptProvider";
    
    private FingerprintObservable fingerprintObservable;

    BiometricPromptProvider biometricPromptProvider = null;

    public static BiometricPromptProvider with(FingerprintObservable fingerprintObservable){
        return new BiometricPromptProvider(fingerprintObservable);
    }
    
    public BiometricPromptProvider(FingerprintObservable fingerprintObservable){
        this.fingerprintObservable = fingerprintObservable;
    }

    protected  <T> void authenticate(Context context, BiometricPrompt.CryptoObject cryptoObject,
                                 final ObservableEmitter<T> emitter) {

        if(context instanceof FragmentActivity) {
            BiometricPrompt mBiometricPrompt = new BiometricPrompt(((FragmentActivity)context),
                    UIThreadExecutor.get(),
                    createAuthenticationCallback(emitter));

            // Set prompt info
            BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                    .setDescription("Description")
                    .setTitle("Title")
                    .setSubtitle("Subtitle")
                    .setNegativeButtonText("Cancel")
                    .build();

            // Show biometric prompt
            if (cryptoObject != null) {
                Log.i(TAG, "Show biometric prompt");
                mBiometricPrompt.authenticate(promptInfo,cryptoObject);
            }
        }
    }


    private <T> BiometricPrompt.AuthenticationCallback createAuthenticationCallback(final ObservableEmitter<T> emitter) {
        return new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errMsgId, CharSequence errString) {
                if (!emitter.isDisposed()) {
                    emitter.onError(new FingerprintAuthenticationException(errString));
                }
            }

            @Override
            public void onAuthenticationFailed() {
                fingerprintObservable.onAuthenticationFailed(emitter);
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                fingerprintObservable.onAuthenticationSucceeded(emitter, result);
            }
        };
    }
}
