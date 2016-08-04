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

package com.mtramin.rxfingerprint.observables;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat.AuthenticationCallback;
import android.support.v4.os.CancellationSignal;

import com.mtramin.rxfingerprint.RxFingerprint;
import com.mtramin.rxfingerprint.data.FingerprintAuthenticationException;

import rx.Observable;
import rx.Subscriber;
import rx.subscriptions.Subscriptions;

/**
 * Base observable for Fingerprint authentication. Provides abstract methods that allow
 * to alter the input and result of the authentication.
 */
public abstract class FingerprintObservable<T> implements Observable.OnSubscribe<T> {

    protected final Context context;
    private CancellationSignal cancellationSignal;

    /**
     * Default constructor for fingerprint authentication
     *
     * @param context Context to be used for the fingerprint authentication
     */
    protected FingerprintObservable(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public void call(final Subscriber<? super T> subscriber) {
        if (!RxFingerprint.isAvailable(context)) {
            subscriber.onError(new IllegalAccessException("Fingerprint authentication is not available on this device! Ensure that the device has a Fingerprint sensor and enrolled Fingerprints by calling RxFingerprint#available(Context) first"));
        }

        AuthenticationCallback callback = createAuthenticationCallback(subscriber);
        cancellationSignal = new CancellationSignal();
        FingerprintManagerCompat.CryptoObject cryptoObject = initCryptoObject(subscriber);
        FingerprintManagerCompat.from(context).authenticate(cryptoObject, 0, cancellationSignal, callback, null);

        subscriber.add(Subscriptions.create(() -> {
            if (cancellationSignal != null && !cancellationSignal.isCanceled()) {
                cancellationSignal.cancel();
            }
        }));
    }

    @NonNull
    private AuthenticationCallback createAuthenticationCallback(final Subscriber<? super T> subscriber) {
        return new AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errMsgId, CharSequence errString) {
                super.onAuthenticationError(errMsgId, errString);
                subscriber.onError(new FingerprintAuthenticationException(errString));
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                FingerprintObservable.this.onAuthenticationFailed(subscriber);
            }

            @Override
            public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
                super.onAuthenticationHelp(helpMsgId, helpString);
                FingerprintObservable.this.onAuthenticationHelp(subscriber, helpMsgId, helpString.toString());
            }

            @Override
            public void onAuthenticationSucceeded(FingerprintManagerCompat.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                FingerprintObservable.this.onAuthenticationSucceeded(subscriber, result);
            }
        };
    }

    /**
     * Method to initialize the {@link android.support.v4.hardware.fingerprint.FingerprintManagerCompat.CryptoObject}
     * used for the fingerprint authentication.
     *
     * @param subscriber current subscriber
     * @return a {@link android.support.v4.hardware.fingerprint.FingerprintManagerCompat.CryptoObject}
     * that is to be used in the authentication. May be {@code null}.
     */
    @Nullable
    protected abstract FingerprintManagerCompat.CryptoObject initCryptoObject(Subscriber<? super T> subscriber);

    /**
     * Action to execute when fingerprint authentication was successful.
     * Should return the needed result via the given {@link Subscriber}.
     * <p/>
     * Should call {@link Subscriber#onCompleted()}.
     *
     * @param subscriber current subscriber
     * @param result     result of the successful fingerprint authentication
     */
    protected abstract void onAuthenticationSucceeded(Subscriber<? super T> subscriber, FingerprintManagerCompat.AuthenticationResult result);

    /**
     * Action to execute when the fingerprint authentication returned a help result.
     * Should return the needed actions to the subscriber via the given {@link Subscriber}.
     * <p/>
     * Should <b>not</b> {@link Subscriber#onCompleted()}.
     *
     * @param subscriber    current subscriber
     * @param helpMessageId ID of the help message returned from the {@link FingerprintManagerCompat}
     * @param helpString    Help message string returned by the {@link FingerprintManagerCompat}
     */
    protected abstract void onAuthenticationHelp(Subscriber<? super T> subscriber, int helpMessageId, String helpString);

    /**
     * Action to execute when the fingerprint authentication failed.
     * Should return the needed action to the given {@link Subscriber}
     * <p/>
     * Should only call {@link Subscriber#onCompleted()} when fingerprint authentication should be
     * canceled due to the failed event.
     *
     * @param subscriber current subscriber
     */
    protected abstract void onAuthenticationFailed(Subscriber<? super T> subscriber);
}
