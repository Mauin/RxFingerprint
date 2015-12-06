package com.mtramin.rxfingerprint.observables;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat.AuthenticationCallback;
import android.support.v4.os.CancellationSignal;

import com.mtramin.rxfingerprint.data.FingerprintAuthenticationException;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;

/**
 * TODO: JAVADOC
 */
public abstract class FingerprintObservable<T> implements Observable.OnSubscribe<T> {

    private final Context context;
    private CancellationSignal cancellationSignal;

    FingerprintObservable(Context context) {
        this.context = context;
    }

    @Override
    public void call(final Subscriber<? super T> subscriber) {
        AuthenticationCallback callback = createAuthenticationCallback(subscriber);
        cancellationSignal = new CancellationSignal();
        FingerprintManagerCompat.CryptoObject cryptoObject = initCryptoObject(subscriber);
        FingerprintManagerCompat.from(context).authenticate(cryptoObject, 0, cancellationSignal, callback, null);

        subscriber.add(Subscriptions.create(new Action0() {
            @Override
            public void call() {
                if (cancellationSignal != null && !cancellationSignal.isCanceled()) {
                    cancellationSignal.cancel();
                }
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

    @Nullable
    protected abstract FingerprintManagerCompat.CryptoObject initCryptoObject(Subscriber<? super T> subscriber);
    protected abstract void onAuthenticationSucceeded(Subscriber<? super T> subscriber, FingerprintManagerCompat.AuthenticationResult result);
    protected abstract void onAuthenticationHelp(Subscriber<? super T> subscriber, int helpMessageId, String helpString);
    protected abstract void onAuthenticationFailed(Subscriber<? super T> subscriber);
}
