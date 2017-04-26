package com.mtramin.rxfingerprint;

import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.util.Log;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Tests for various helper methods included in {@link RxFingerprint}
 */
@SuppressWarnings({"NewApi", "MissingPermission"})
@RunWith(PowerMockRunner.class)
@PrepareForTest(FingerprintManager.class)
public class RxFingerprintTest {

    @Mock Context mockContext;
    @Mock FingerprintManager mockFingerprintManager;

    @Before
    public void setUp() throws Exception {
        RxFingerprint.disableLogging();
        initMocks(this);
        TestHelper.setSdkLevel(23);
        PowerMockito.mockStatic(FingerprintManager.class);
        PowerMockito.mockStatic(Log.class);
    }

    @Test
    public void testKeyInvalidatedException() throws Exception {
        Throwable throwable = new KeyPermanentlyInvalidatedException();
        assertTrue("Should result to true", RxFingerprint.keyInvalidated(throwable));
    }

    @Test
    public void testAvailable() throws Exception {
        when(mockContext.getSystemService(Context.FINGERPRINT_SERVICE)).thenReturn(mockFingerprintManager);
        when(mockFingerprintManager.isHardwareDetected()).thenReturn(true);
        when(mockFingerprintManager.hasEnrolledFingerprints()).thenReturn(true);

        assertTrue("RxFingerprint should be available", RxFingerprint.isAvailable(mockContext));
        assertFalse("RxFingerprint should be available", RxFingerprint.isUnavailable(mockContext));
    }

    @Test
    public void testUnavailableWithNoHardware() throws Exception {
        when(mockContext.getSystemService(Context.FINGERPRINT_SERVICE)).thenReturn(mockFingerprintManager);
        when(mockFingerprintManager.isHardwareDetected()).thenReturn(false);
        when(mockFingerprintManager.hasEnrolledFingerprints()).thenReturn(true);

        assertFalse("RxFingerprint should be unavailable", RxFingerprint.isAvailable(mockContext));
        assertTrue("RxFingerprint should be unavailable", RxFingerprint.isUnavailable(mockContext));
    }

    @Test
    public void testUnavailableWithNoFingerprint() throws Exception {
        when(mockContext.getSystemService(Context.FINGERPRINT_SERVICE)).thenReturn(mockFingerprintManager);
        when(mockFingerprintManager.isHardwareDetected()).thenReturn(true);
        when(mockFingerprintManager.hasEnrolledFingerprints()).thenReturn(false);

        assertFalse("RxFingerprint should be unavailable", RxFingerprint.isAvailable(mockContext));
        assertTrue("RxFingerprint should be unavailable", RxFingerprint.isUnavailable(mockContext));
    }

    @Test
    public void testUnavailable() throws Exception {
        when(mockContext.getSystemService(Context.FINGERPRINT_SERVICE)).thenReturn(mockFingerprintManager);
        when(mockFingerprintManager.isHardwareDetected()).thenReturn(false);
        when(mockFingerprintManager.hasEnrolledFingerprints()).thenReturn(false);

        assertFalse("RxFingerprint should be unavailable", RxFingerprint.isAvailable(mockContext));
        assertTrue("RxFingerprint should be unavailable", RxFingerprint.isUnavailable(mockContext));
    }

    @Test
    public void fingerprintAvailable() throws Exception {
        when(mockContext.getSystemService(Context.FINGERPRINT_SERVICE)).thenReturn(mockFingerprintManager);
        when(mockFingerprintManager.hasEnrolledFingerprints()).thenReturn(true);

        assertTrue("Fingerprint should be available", RxFingerprint.hasEnrolledFingerprints(mockContext));
    }

    @Test
    public void hardwareAvailable() throws Exception {
        when(mockContext.getSystemService(Context.FINGERPRINT_SERVICE)).thenReturn(mockFingerprintManager);
        when(mockFingerprintManager.isHardwareDetected()).thenReturn(true);

        assertTrue("Hardware should be available", RxFingerprint.isHardwareDetected(mockContext));
    }

    @Test
    public void fingerprintUnavailable() throws Exception {
        when(mockContext.getSystemService(Context.FINGERPRINT_SERVICE)).thenReturn(mockFingerprintManager);
        when(mockFingerprintManager.hasEnrolledFingerprints()).thenReturn(false);

        assertFalse("Fingerprint should not be unavailable", RxFingerprint.hasEnrolledFingerprints(mockContext));
    }

    @Test
    public void hardwareUnavailable() throws Exception {
        when(mockContext.getSystemService(Context.FINGERPRINT_SERVICE)).thenReturn(mockFingerprintManager);
        when(mockFingerprintManager.isHardwareDetected()).thenReturn(false);

        assertFalse("Hardware should not be available", RxFingerprint.isHardwareDetected(mockContext));
    }

    @Test
    public void apisUnavailable() throws Exception {
        when(mockContext.getSystemService(Context.FINGERPRINT_SERVICE)).thenThrow(new NoClassDefFoundError());

        assertFalse("RxFingerprint should be unavailable", RxFingerprint.isAvailable(mockContext));
    }

    @Test
    public void sdkNotSupported() throws Exception {
        TestHelper.setSdkLevel(21);
        assertFalse("RxFingerprint should be unavailable", RxFingerprint.isAvailable(mockContext));
    }
}
