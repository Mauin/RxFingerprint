package com.mtramin.rxfingerprint.observables;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;

import com.mtramin.rxfingerprint.data.CryptoData;
import com.mtramin.rxfingerprint.data.FingerprintDecryptionResult;
import com.mtramin.rxfingerprint.data.FingerprintResult;
import com.mtramin.rxfingerprint.utils.CryptoUtils;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import rx.Observable;
import rx.Subscriber;

/**
 * TODO: JAVADOC
 */
public class FingerprintDecryptionObservable extends FingerprintObservable<FingerprintDecryptionResult> {

    private CryptoData encryptedData;

    private FingerprintDecryptionObservable(Context context, String encrypted) {
        super(context);
        this.encryptedData = CryptoData.fromString(encrypted);
    }

    public static Observable<FingerprintDecryptionResult> create(Context context, String encrypted) {
        return Observable.create(new FingerprintDecryptionObservable(context, encrypted));
    }

    @Nullable
    @Override
    protected FingerprintManagerCompat.CryptoObject initCryptoObject(Subscriber<? super FingerprintDecryptionResult> subscriber) {
        try {
            Cipher cipher = CryptoUtils.initDecryptionCipher(encryptedData.getIv());
            return new FingerprintManagerCompat.CryptoObject(cipher);

        } catch (NoSuchAlgorithmException | CertificateException | InvalidKeyException | KeyStoreException | InvalidAlgorithmParameterException | NoSuchPaddingException | IOException | UnrecoverableKeyException e) {
            subscriber.onError(e);
            return null;
        }
    }

    @Override
    protected void onAuthenticationSucceeded(Subscriber<? super FingerprintDecryptionResult> subscriber, FingerprintManagerCompat.AuthenticationResult result) {
        try {
            Cipher cipher = result.getCryptoObject().getCipher();
            String decrypted = new String(cipher.doFinal(encryptedData.getMessage()));

            subscriber.onNext(new FingerprintDecryptionResult(FingerprintResult.AUTHENTICATED, null, decrypted));
            subscriber.onCompleted();
        } catch (BadPaddingException | IllegalBlockSizeException e) {
            subscriber.onError(e);
        }

    }

    @Override
    protected void onAuthenticationHelp(Subscriber<? super FingerprintDecryptionResult> subscriber, int helpMessageId, String helpString) {
        subscriber.onNext(new FingerprintDecryptionResult(FingerprintResult.HELP, helpString, null));
    }

    @Override
    protected void onAuthenticationFailed(Subscriber<? super FingerprintDecryptionResult> subscriber) {
        subscriber.onNext(new FingerprintDecryptionResult(FingerprintResult.FAILED, null, null));
    }
}
