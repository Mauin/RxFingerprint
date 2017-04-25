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
import android.hardware.fingerprint.FingerprintManager.AuthenticationResult;
import android.hardware.fingerprint.FingerprintManager.CryptoObject;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

import com.mtramin.rxfingerprint.data.FingerprintAuthenticationResult;
import com.mtramin.rxfingerprint.data.FingerprintResult;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;

/**
 * Authenticates the user with his fingerprint.
 */
class FingerprintAuthenticationObservable extends FingerprintObservable<FingerprintAuthenticationResult> {

    /**
     * Creates an Observable that will enable the fingerprint scanner of the device and listen for
     * the users fingerprint for authentication
     *
     * @param context context to use
     * @return Observable {@link FingerprintAuthenticationResult}
     */
    static Observable<FingerprintAuthenticationResult> create(Context context) {
        return Observable.create(new FingerprintAuthenticationObservable(new FingerprintApiWrapper(context)));
    }

    @VisibleForTesting
	FingerprintAuthenticationObservable(FingerprintApiWrapper fingerprintApiWrapper) {
		super(fingerprintApiWrapper);
	}

    @Nullable
    @Override
    protected CryptoObject initCryptoObject(ObservableEmitter<FingerprintAuthenticationResult> subscriber) {
        // Simple authentication does not need CryptoObject
        return null;
    }

    @Override
    protected void onAuthenticationSucceeded(ObservableEmitter<FingerprintAuthenticationResult> emitter, AuthenticationResult result) {
        emitter.onNext(new FingerprintAuthenticationResult(FingerprintResult.AUTHENTICATED, null));
        emitter.onComplete();
    }

    @Override
    protected void onAuthenticationHelp(ObservableEmitter<FingerprintAuthenticationResult> emitter, int helpMessageId, String helpString) {
        emitter.onNext(new FingerprintAuthenticationResult(FingerprintResult.HELP, helpString));
    }

    @Override
    protected void onAuthenticationFailed(ObservableEmitter<FingerprintAuthenticationResult> emitter) {
        emitter.onNext(new FingerprintAuthenticationResult(FingerprintResult.FAILED, null));
    }
}
