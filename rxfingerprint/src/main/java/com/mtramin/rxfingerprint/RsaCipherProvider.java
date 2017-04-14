package com.mtramin.rxfingerprint;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.spec.KeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

class RsaCipherProvider extends CipherProvider {

	static Cipher forEncryption(@NonNull Context context, @Nullable String keyName) throws IOException, GeneralSecurityException {
		return new RsaCipherProvider(context, keyName).getCipherForEncryption();
	}

	static Cipher forDecryption(@NonNull Context context, @Nullable String keyName) throws GeneralSecurityException, IOException {
		return new RsaCipherProvider(context, keyName).getCipherForDecryption();
	}

	private RsaCipherProvider(@NonNull Context context, @Nullable String keyName) throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {
		super(context, keyName);
	}

	@Override
	@TargetApi(Build.VERSION_CODES.M)
	Cipher cipherForEncryption() throws GeneralSecurityException, IOException {
		KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, ANDROID_KEY_STORE);

		keyGenerator.initialize(new KeyGenParameterSpec.Builder(keyName,
				KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
				.setBlockModes(KeyProperties.BLOCK_MODE_ECB)
				.setUserAuthenticationRequired(true)
				.setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
				.build());

		keyGenerator.generateKeyPair();

		KeyFactory keyFactory = KeyFactory.getInstance(KeyProperties.KEY_ALGORITHM_RSA);
		Cipher cipher = createCipher();
		cipher.init(Cipher.ENCRYPT_MODE, getPublicKey(keyFactory, keyStore));

		return cipher;
	}

	private Cipher getCipherForDecryption() throws GeneralSecurityException {
		Cipher cipher = createCipher();
		cipher.init(Cipher.DECRYPT_MODE, getPrivateKey(keyStore, keyName));
		return cipher;
	}

	private PrivateKey getPrivateKey(KeyStore keyStore, String keyAlias) throws GeneralSecurityException {
		return (PrivateKey) keyStore.getKey(keyAlias, null);
	}

	private PublicKey getPublicKey(KeyFactory keyFactory, KeyStore keyStore) throws GeneralSecurityException {
		PublicKey publicKey = keyStore.getCertificate(keyName).getPublicKey();
		KeySpec spec = new X509EncodedKeySpec(publicKey.getEncoded());
		return keyFactory.generatePublic(spec);
	}

	@Override
	@TargetApi(Build.VERSION_CODES.M)
	Cipher createCipher() throws NoSuchPaddingException, NoSuchAlgorithmException {
		return Cipher.getInstance(KeyProperties.KEY_ALGORITHM_RSA + "/"
				+ KeyProperties.BLOCK_MODE_ECB + "/"
				+ KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1);
	}
}
