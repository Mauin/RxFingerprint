package com.mtramin.rxfingerprint.observables;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;

import com.mtramin.rxfingerprint.data.CryptoData;
import com.mtramin.rxfingerprint.data.FingerprintEncryptionResult;
import com.mtramin.rxfingerprint.data.FingerprintResult;
import com.mtramin.rxfingerprint.utils.CryptoUtils;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidParameterSpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;

import rx.Observable;
import rx.Subscriber;

/**
 * Encrypts data with fingerprint authentication. Initializes a {@link Cipher} for encryption which
 * can only be used with fingerprint authentication and uses it once authentication was successful
 * to encrypt the given data.
 */
public class FingerprintEncryptionObservable extends FingerprintObservable<FingerprintEncryptionResult> {

    private String toEncrypt;

    private FingerprintEncryptionObservable(Context context, String toEncrypt) {
        super(context);
        this.toEncrypt = toEncrypt;
    }

    /**
     * Creates a new FingerprintEncryptionObservable that will listen to fingerprint authentication
     * to encrypt the given data.
     *
     * @param context   context to use
     * @param toEncrypt data to encrypt
     * @return Observable {@link FingerprintEncryptionResult}
     */
    public static Observable<FingerprintEncryptionResult> create(Context context, String toEncrypt) {
        return Observable.create(new FingerprintEncryptionObservable(context, toEncrypt));
    }

    @Nullable
    @Override
    protected FingerprintManagerCompat.CryptoObject initCryptoObject(Subscriber<? super FingerprintEncryptionResult> subscriber) {
        try {
            Cipher cipher = CryptoUtils.initEncryptionCipher();
            return new FingerprintManagerCompat.CryptoObject(cipher);
        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchProviderException | NoSuchPaddingException | InvalidAlgorithmParameterException e) {
            subscriber.onError(e);
            return null;
        }

    }

    @Override
    protected void onAuthenticationSucceeded(Subscriber<? super FingerprintEncryptionResult> subscriber, FingerprintManagerCompat.AuthenticationResult result) {
        try {
            Cipher cipher = result.getCryptoObject().getCipher();
            byte[] encryptedBytes = cipher.doFinal(toEncrypt.getBytes("UTF-8"));
            byte[] ivBytes = cipher.getParameters().getParameterSpec(IvParameterSpec.class).getIV();

            CryptoData cryptoData = CryptoData.fromBytes(encryptedBytes, ivBytes);

            subscriber.onNext(new FingerprintEncryptionResult(FingerprintResult.AUTHENTICATED, null, cryptoData.toString()));
            subscriber.onCompleted();
        } catch (IllegalBlockSizeException | BadPaddingException | InvalidParameterSpecException | UnsupportedEncodingException e) {
            subscriber.onError(e);
        }
    }

    @Override
    protected void onAuthenticationHelp(Subscriber<? super FingerprintEncryptionResult> subscriber, int helpMessageId, String helpString) {
        subscriber.onNext(new FingerprintEncryptionResult(FingerprintResult.HELP, helpString, null));
    }

    @Override
    protected void onAuthenticationFailed(Subscriber<? super FingerprintEncryptionResult> subscriber) {
        subscriber.onNext(new FingerprintEncryptionResult(FingerprintResult.FAILED, null, null));
    }
}
