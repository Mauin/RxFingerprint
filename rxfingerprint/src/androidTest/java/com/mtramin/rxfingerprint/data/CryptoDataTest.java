package com.mtramin.rxfingerprint.data;

import android.util.Base64;

import com.mtramin.rxfingerprint.CryptoData;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertTrue;

/**
 * Tests for the {@link CryptoData} data, which includes Base64 encoding and decoding
 */
public class CryptoDataTest {

    private static final byte[] TEST_MESSAGE = "testMessage".getBytes();
    private static final byte[] TEST_IV = "testIv".getBytes();

    public CryptoDataTest() {
    }

    @Test
    public void encode() throws Exception {
        CryptoData cryptoData = CryptoData.fromBytes(TEST_MESSAGE, TEST_IV);

        assertTrue("Encoded String should contain seperator!", cryptoData.toString().contains("-_-"));
        assertTrue("Message should be the same", Arrays.equals(cryptoData.getMessage(), TEST_MESSAGE));
        assertTrue("IV should be the same", Arrays.equals(cryptoData.getIv(), TEST_IV));
    }

    @Test
    public void decode() throws Exception {
        String message = Base64.encodeToString(TEST_MESSAGE, 0);
        String iv = Base64.encodeToString(TEST_IV, 0);

        CryptoData cryptoData = CryptoData.fromString(message + "-_-" + iv);
        String cryptoString = cryptoData.toString();

        assertTrue("Encoded String should contain seperator!", cryptoString.contains("-_-") && cryptoString.startsWith(message) && cryptoString.endsWith(iv));
        assertTrue("Message should be the same", Arrays.equals(cryptoData.getMessage(), TEST_MESSAGE));
        assertTrue("IV should be the same", Arrays.equals(cryptoData.getIv(), TEST_IV));
    }
}