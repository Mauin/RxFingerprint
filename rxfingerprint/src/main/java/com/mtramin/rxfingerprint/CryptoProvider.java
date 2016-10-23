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

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Enumeration;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

/**
 * Provider class for cryptographic elements used in the encryption/decryption
 * of {@link RxFingerprint}
 */
class CryptoProvider {

    private static final String ANDROID_KEY_STORE = "AndroidKeyStore";
    private static final String DEFAULT_KEY_NAME = "rxfingerprint_default";
    private static final int DEFAULT_KEY_SIZE = 256;

    private final String keyName;

    /**
     * Creates a new CryptoProvider. If a keyName is provided, uses the given key in the KeyStore
     * for cryptographic operations. The given key name is {@code null} a default key name will be
     * used.
     * <p/>
     * The default key name will consist of the applications package name appended with
     * {@link CryptoProvider#DEFAULT_KEY_NAME}.
     *
     * @param context context to use, may not be null
     * @param keyName keyName to use, can be null
     */
    CryptoProvider(@NonNull Context context, @Nullable String keyName) {
        if (keyName == null) {
            this.keyName = ContextUtils.getPackageName(context) + "." + DEFAULT_KEY_NAME;
        } else {
            this.keyName = keyName;
        }
    }

    /**
     * @return Initialized cipher for encryption operations in RxFinerprint
     */
    @TargetApi(Build.VERSION_CODES.M)
    Cipher initEncryptionCipher() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, NoSuchProviderException, InvalidAlgorithmParameterException, UnrecoverableKeyException, CertificateException, KeyStoreException, IOException {
        Cipher cipher = createCipher();
        SecretKey key = findOrCreateKey(keyName);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher;
    }

    /**
     * @param iv initialization vector used during encryption
     * @return Initialized cipher for decryption operations in RxFingerprint
     */
    Cipher initDecryptionCipher(byte[] iv) throws CertificateException, NoSuchAlgorithmException, IOException, InvalidKeyException, UnrecoverableKeyException, KeyStoreException, InvalidAlgorithmParameterException, NoSuchPaddingException {
        Cipher cipher = createCipher();
        SecretKey key = getKey(keyName);
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
        return cipher;
    }

    @TargetApi(Build.VERSION_CODES.M)
    private Cipher createCipher() throws NoSuchPaddingException, NoSuchAlgorithmException {
        return Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                + KeyProperties.BLOCK_MODE_CBC + "/"
                + KeyProperties.ENCRYPTION_PADDING_PKCS7);
    }

    private SecretKey findOrCreateKey(String keyName) throws NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, UnrecoverableKeyException, CertificateException, KeyStoreException, IOException {
        if (keyExists(keyName)) {
            return getKey(keyName);
        }
        return createKey(keyName);
    }

    private boolean keyExists(String keyName) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
        keyStore.load(null);
        Enumeration<String> aliases = keyStore.aliases();

        while (aliases.hasMoreElements()) {
            if (keyName.equals(aliases.nextElement())) {
                return true;
            }
        }

        return false;
    }

    @TargetApi(Build.VERSION_CODES.M)
    private SecretKey createKey(String keyName) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE);
        keyGenerator.init(new KeyGenParameterSpec.Builder(keyName,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setKeySize(DEFAULT_KEY_SIZE)
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setUserAuthenticationRequired(true)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .build());
        return keyGenerator.generateKey();
    }

    private SecretKey getKey(String keyName) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, UnrecoverableKeyException {
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
        keyStore.load(null);
        return (SecretKey) keyStore.getKey(keyName, null);
    }
}
