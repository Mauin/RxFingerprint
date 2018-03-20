/*
 * Copyright 2018 Marvin Ramin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * <http://www.apache.org/licenses/LICENSE-2.0>
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mtramin.rxfingerprint;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.hardware.fingerprint.FingerprintDialog;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.RequiresPermission;
import android.util.Log;

import com.mtramin.rxfingerprint.data.FingerprintAuthenticationException;

import java.util.concurrent.Executor;

import io.reactivex.Emitter;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.functions.Cancellable;

import static android.Manifest.permission.USE_FINGERPRINT;

/**
 * Authenticates the user with their fingerprint via a {@link FingerprintDialog}.
 */
@SuppressLint({"NewApi", "Override"})
abstract class FingerprintDialogObservable<T> implements ObservableOnSubscribe<T> {

    private final FingerprintApiWrapper fingerprintApiWrapper;
    private final FingerprintDialogBundle fingerprintDialogBundle;
    CancellationSignal cancellationSignal;

    FingerprintDialogObservable(FingerprintApiWrapper fingerprintApiWrapper, FingerprintDialogBundle fingerprintDialogBundle) {
        this.fingerprintApiWrapper = fingerprintApiWrapper;
        this.fingerprintDialogBundle = fingerprintDialogBundle;
    }

    @Override
    @RequiresPermission(USE_FINGERPRINT)
    @RequiresApi(Build.VERSION_CODES.P)
    public void subscribe(ObservableEmitter<T> emitter) throws Exception {
        Executor executor = new Executor() {
            @Override
            public void execute(@NonNull Runnable runnable) {
                runnable.run();
            }
        };

        FingerprintDialog.AuthenticationCallback authenticationCallback = createAuthenticationCallback(emitter);
        cancellationSignal = fingerprintApiWrapper.createCancellationSignal();
        FingerprintDialog.CryptoObject cryptoObject = initCryptoObject(emitter);
        FingerprintDialog.Builder builder = new FingerprintDialog.Builder()
                .setTitle(fingerprintDialogBundle.getDialogTitleText())
                .setSubtitle(fingerprintDialogBundle.getDialogSubtitleText())
                .setDescription(fingerprintDialogBundle.getDialogDescriptionText())
                .setNegativeButton(fingerprintDialogBundle.getDialogNegativeButtonText(), executor, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.e("RxFingerprint", "Dialog cancelled!");
                        cancellationSignal.cancel();
                    }
                });

        FingerprintDialog fingerprintDialog = fingerprintApiWrapper.buildDialog(builder);
        if (cryptoObject == null) {
            fingerprintDialog.authenticate(cancellationSignal, executor, authenticationCallback);
        } else {
            fingerprintDialog.authenticate(cryptoObject, cancellationSignal, executor, authenticationCallback);
        }

        emitter.setCancellable(new Cancellable() {
            @Override
            public void cancel() throws Exception {
                cancellationSignal.cancel();
            }
        });
    }

    private FingerprintDialog.AuthenticationCallback createAuthenticationCallback(final ObservableEmitter<T> emitter) {
        return new FingerprintDialog.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errMsgId, CharSequence errString) {
                if (!emitter.isDisposed()) {
                    emitter.onError(new FingerprintAuthenticationException(errString));
                }
            }

            @Override
            public void onAuthenticationFailed() {
                FingerprintDialogObservable.this.onAuthenticationFailed(emitter);
            }

            @Override
            public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
                FingerprintDialogObservable.this.onAuthenticationHelp(emitter, helpMsgId, helpString.toString());
            }

            @Override
            public void onAuthenticationSucceeded(FingerprintDialog.AuthenticationResult result) {
                FingerprintDialogObservable.this.onAuthenticationSucceeded(emitter, result);
            }
        };
    }

    /**
     * Method to initialize the {@link FingerprintManager.CryptoObject}
     * used for the fingerprint authentication.
     *
     * @param subscriber current subscriber
     * @return a {@link FingerprintManager.CryptoObject}
     * that is to be used in the authentication. May be {@code null}.
     */
    @Nullable
    protected abstract FingerprintDialog.CryptoObject initCryptoObject(ObservableEmitter<T> subscriber);

    /**
     * Action to execute when fingerprint authentication was successful.
     * Should return the needed result via the given {@link Emitter}.
     * <p/>
     * Should call {@link Emitter#onComplete()}.
     *
     * @param emitter current subscriber
     * @param result  result of the successful fingerprint authentication
     */
    protected abstract void onAuthenticationSucceeded(ObservableEmitter<T> emitter, FingerprintDialog.AuthenticationResult result);

    /**
     * Action to execute when the fingerprint authentication returned a help result.
     * Should return the needed actions to the subscriber via the given {@link Emitter}.
     * <p/>
     * Should <b>not</b> {@link Emitter#onComplete()}.
     *
     * @param emitter       current subscriber
     * @param helpMessageId ID of the help message returned from the {@link FingerprintManager}
     * @param helpString    Help message string returned by the {@link FingerprintManager}
     */
    protected abstract void onAuthenticationHelp(ObservableEmitter<T> emitter, int helpMessageId, String helpString);

    /**
     * Action to execute when the fingerprint authentication failed.
     * Should return the needed action to the given {@link Emitter}.
     * <p/>
     * Should only call {@link Emitter#onComplete()} when fingerprint authentication should be
     * canceled due to the failed event.
     *
     * @param emitter current subscriber
     */
    protected abstract void onAuthenticationFailed(ObservableEmitter<T> emitter);
}
