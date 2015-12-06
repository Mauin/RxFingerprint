package com.mtramin.rxfingerprint.utils;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

/**
 * TODO: JAVADOC
 */
public class CryptoUtils {

    /**
     * Alias for our key in the Android Key Store
     */
    // TODO make this user changable
    private static final String KEY_NAME = "my_key";

    public static Cipher createCipher() throws NoSuchPaddingException, NoSuchAlgorithmException {
        return Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                + KeyProperties.BLOCK_MODE_CBC + "/"
                + KeyProperties.ENCRYPTION_PADDING_PKCS7);
    }

    public static Cipher initEncryptionCipher() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, NoSuchProviderException, InvalidAlgorithmParameterException {
        Cipher cipher = createCipher();
        SecretKey key = createKey();
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher;
    }

    private static SecretKey createKey() throws NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        keyGenerator.init(new KeyGenParameterSpec.Builder(KEY_NAME,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setUserAuthenticationRequired(true)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .build());
        return keyGenerator.generateKey();
    }

    public static Cipher initDecryptionCipher(byte[] iv) throws CertificateException, NoSuchAlgorithmException, IOException, InvalidKeyException, UnrecoverableKeyException, KeyStoreException, InvalidAlgorithmParameterException, NoSuchPaddingException {
        Cipher cipher = createCipher();
        SecretKey key = getKey();
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
        return cipher;
    }

    private static SecretKey getKey() throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, UnrecoverableKeyException {
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        return (SecretKey) keyStore.getKey(KEY_NAME, null);
    }
}
