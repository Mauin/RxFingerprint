package com.mtramin.rxfingerprint;

import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.lang.reflect.Field;
import java.security.KeyStore;

import javax.crypto.Cipher;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

/**
 * Basic tests for the {@link CryptoProvider}
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Cipher.class, KeyStore.class})
public class CryptoProviderTest {

    private static final String FIELD_KEY_NAME = "keyName";
    private static final String PACKAGE_NAME = "com.rxfingerprint.test";

    @Mock
    Context mockContext;

    @Mock
    Cipher mockCipher;

    @Mock
    KeyStore mockKeyStore;

    @Before
    public void setUp() throws Exception {
        initMocks(this);

        mockStatic(Cipher.class);
        mockStatic(KeyStore.class);

        when(mockContext.getPackageName()).thenReturn(PACKAGE_NAME);
    }

    @Test
    public void testDefaultKeyName() throws Exception {
        CryptoProvider cryptoProvider = new CryptoProvider(mockContext, null);

        String keyName = getPrivateStringField(cryptoProvider, FIELD_KEY_NAME);
        assertTrue("Key should contain the package name plus some default value", keyName.startsWith(PACKAGE_NAME) && keyName.replace(PACKAGE_NAME, "").length() > 0);
    }

    @Test
    public void testKeyName() throws Exception {
        CryptoProvider cryptoProvider = new CryptoProvider(mockContext, PACKAGE_NAME);

        String keyName = getPrivateStringField(cryptoProvider, FIELD_KEY_NAME);
        assertTrue("Key should equal constructor argument", keyName.equals(PACKAGE_NAME));
    }

    private <T> String getPrivateStringField(T t, String fieldName) throws Exception {
        Field privateField = t.getClass().getDeclaredField(fieldName);
        privateField.setAccessible(true);
        return (String) privateField.get(t);
    }
}