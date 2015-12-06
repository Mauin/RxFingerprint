package com.mtramin.rxfingerprint.observables;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;

import com.mtramin.rxfingerprint.data.FingerprintAuthenticationResult;
import com.mtramin.rxfingerprint.data.FingerprintResult;

import rx.Observable;
import rx.Subscriber;

/**
 * TODO: JAVADOC
 */
public class FingerprintAuthenticationObservable extends FingerprintObservable<FingerprintAuthenticationResult> {

    private FingerprintAuthenticationObservable(Context context) {
        super(context);
    }

    public static Observable<FingerprintAuthenticationResult> create(Context context) {
        return Observable.create(new FingerprintAuthenticationObservable(context));
    }

    @Nullable
    @Override
    protected FingerprintManagerCompat.CryptoObject initCryptoObject(Subscriber<? super FingerprintAuthenticationResult> subscriber) {
        // Simple authentication does not need CryptoObject
        return null;
    }

    @Override
    protected void onAuthenticationSucceeded(Subscriber<? super FingerprintAuthenticationResult> subscriber, FingerprintManagerCompat.AuthenticationResult result) {
        subscriber.onNext(new FingerprintAuthenticationResult(FingerprintResult.AUTHENTICATED, null));
        subscriber.onCompleted();
    }

    @Override
    protected void onAuthenticationHelp(Subscriber<? super FingerprintAuthenticationResult> subscriber, int helpMessageId, String helpString) {
        subscriber.onNext(new FingerprintAuthenticationResult(FingerprintResult.HELP, helpString));
    }

    @Override
    protected void onAuthenticationFailed(Subscriber<? super FingerprintAuthenticationResult> subscriber) {
        subscriber.onNext(new FingerprintAuthenticationResult(FingerprintResult.FAILED, null));
    }
}
