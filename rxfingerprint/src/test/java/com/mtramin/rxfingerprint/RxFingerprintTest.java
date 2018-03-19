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

    private RxFingerprint rxFingerprint;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        TestHelper.setSdkLevel(23);
        PowerMockito.mockStatic(FingerprintManager.class);
        PowerMockito.mockStatic(Log.class);

        rxFingerprint = new RxFingerprint.Builder(mockContext)
                .disableLogging()
                .build();
    }

    @Test
    public void testKeyInvalidatedException() throws Exception {
        Throwable throwable = new KeyPermanentlyInvalidatedException();
        assertTrue("Should result to true", rxFingerprint.keyInvalidated(throwable));
    }

    @Test
    public void testAvailable() throws Exception {
        when(mockContext.getSystemService(Context.FINGERPRINT_SERVICE)).thenReturn(mockFingerprintManager);
        when(mockFingerprintManager.isHardwareDetected()).thenReturn(true);
        when(mockFingerprintManager.hasEnrolledFingerprints()).thenReturn(true);

        assertTrue("RxFingerprint should be available", rxFingerprint.isAvailable());
        assertFalse("RxFingerprint should be available", rxFingerprint.isUnavailable());
    }

    @Test
    public void testUnavailableWithNoHardware() throws Exception {
        when(mockContext.getSystemService(Context.FINGERPRINT_SERVICE)).thenReturn(mockFingerprintManager);
        when(mockFingerprintManager.isHardwareDetected()).thenReturn(false);
        when(mockFingerprintManager.hasEnrolledFingerprints()).thenReturn(true);

        assertFalse("RxFingerprint should be unavailable", rxFingerprint.isAvailable());
        assertTrue("RxFingerprint should be unavailable", rxFingerprint.isUnavailable());
    }

    @Test
    public void testUnavailableWithNoFingerprint() throws Exception {
        when(mockContext.getSystemService(Context.FINGERPRINT_SERVICE)).thenReturn(mockFingerprintManager);
        when(mockFingerprintManager.isHardwareDetected()).thenReturn(true);
        when(mockFingerprintManager.hasEnrolledFingerprints()).thenReturn(false);

        assertFalse("RxFingerprint should be unavailable", rxFingerprint.isAvailable());
        assertTrue("RxFingerprint should be unavailable", rxFingerprint.isUnavailable());
    }

    @Test
    public void testUnavailable() throws Exception {
        when(mockContext.getSystemService(Context.FINGERPRINT_SERVICE)).thenReturn(mockFingerprintManager);
        when(mockFingerprintManager.isHardwareDetected()).thenReturn(false);
        when(mockFingerprintManager.hasEnrolledFingerprints()).thenReturn(false);

        assertFalse("RxFingerprint should be unavailable", rxFingerprint.isAvailable());
        assertTrue("RxFingerprint should be unavailable", rxFingerprint.isUnavailable());
    }

    @Test
    public void fingerprintAvailable() throws Exception {
        when(mockContext.getSystemService(Context.FINGERPRINT_SERVICE)).thenReturn(mockFingerprintManager);
        when(mockFingerprintManager.hasEnrolledFingerprints()).thenReturn(true);

        assertTrue("Fingerprint should be available", rxFingerprint.hasEnrolledFingerprints());
    }

    @Test
    public void hardwareAvailable() throws Exception {
        when(mockContext.getSystemService(Context.FINGERPRINT_SERVICE)).thenReturn(mockFingerprintManager);
        when(mockFingerprintManager.isHardwareDetected()).thenReturn(true);

        assertTrue("Hardware should be available", rxFingerprint.isHardwareDetected());
    }

    @Test
    public void fingerprintUnavailable() throws Exception {
        when(mockContext.getSystemService(Context.FINGERPRINT_SERVICE)).thenReturn(mockFingerprintManager);
        when(mockFingerprintManager.hasEnrolledFingerprints()).thenReturn(false);

        assertFalse("Fingerprint should not be unavailable", rxFingerprint.hasEnrolledFingerprints());
    }

    @Test
    public void hardwareUnavailable() throws Exception {
        when(mockContext.getSystemService(Context.FINGERPRINT_SERVICE)).thenReturn(mockFingerprintManager);
        when(mockFingerprintManager.isHardwareDetected()).thenReturn(false);

        assertFalse("Hardware should not be available", rxFingerprint.isHardwareDetected());
    }

    @Test
    public void apisUnavailable() throws Exception {
        when(mockContext.getSystemService(Context.FINGERPRINT_SERVICE)).thenThrow(new NoClassDefFoundError());

        assertFalse("RxFingerprint should be unavailable", rxFingerprint.isAvailable());
    }

    @Test
    public void sdkNotSupported() throws Exception {
        TestHelper.setSdkLevel(21);
        assertFalse("RxFingerprint should be unavailable", rxFingerprint.isAvailable());
    }
}
