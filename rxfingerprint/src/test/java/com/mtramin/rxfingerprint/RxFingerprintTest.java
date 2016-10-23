package com.mtramin.rxfingerprint;

import android.content.Context;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;

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
@RunWith(PowerMockRunner.class)
@PrepareForTest({FingerprintManagerCompat.class})
public class RxFingerprintTest {

    @Mock
    Context mockContext;

    @Mock
    FingerprintManagerCompat mockFingerprintManager;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        PowerMockito.mockStatic(FingerprintManagerCompat.class);
    }

    @Test
    public void testKeyInvalidatedException() throws Exception {
        Throwable throwable = new KeyPermanentlyInvalidatedException();
        assertTrue("Should result to true", RxFingerprint.keyInvalidated(throwable));
    }

    @Test
    public void testAvailable() throws Exception {
        when(FingerprintManagerCompat.from(mockContext)).thenReturn(mockFingerprintManager);
        when(mockFingerprintManager.isHardwareDetected()).thenReturn(true);
        when(mockFingerprintManager.hasEnrolledFingerprints()).thenReturn(true);

        assertTrue("RxFingerprint should be available", RxFingerprint.isAvailable(mockContext));
        assertFalse("RxFingerprint should be available", RxFingerprint.isUnavailable(mockContext));
    }

    @Test
    public void testUnavailableWithNoHardware() throws Exception {
        when(FingerprintManagerCompat.from(mockContext)).thenReturn(mockFingerprintManager);
        when(mockFingerprintManager.isHardwareDetected()).thenReturn(false);
        when(mockFingerprintManager.hasEnrolledFingerprints()).thenReturn(true);

        assertFalse("RxFingerprint should be unavailable", RxFingerprint.isAvailable(mockContext));
        assertTrue("RxFingerprint should be unavailable", RxFingerprint.isUnavailable(mockContext));
    }

    @Test
    public void testUnavailableWithNoFingerprint() throws Exception {
        when(FingerprintManagerCompat.from(mockContext)).thenReturn(mockFingerprintManager);
        when(mockFingerprintManager.isHardwareDetected()).thenReturn(true);
        when(mockFingerprintManager.hasEnrolledFingerprints()).thenReturn(false);

        assertFalse("RxFingerprint should be unavailable", RxFingerprint.isAvailable(mockContext));
        assertTrue("RxFingerprint should be unavailable", RxFingerprint.isUnavailable(mockContext));
    }

    @Test
    public void testUnavailable() throws Exception {
        when(FingerprintManagerCompat.from(mockContext)).thenReturn(mockFingerprintManager);
        when(mockFingerprintManager.isHardwareDetected()).thenReturn(false);
        when(mockFingerprintManager.hasEnrolledFingerprints()).thenReturn(false);

        assertFalse("RxFingerprint should be unavailable", RxFingerprint.isAvailable(mockContext));
        assertTrue("RxFingerprint should be unavailable", RxFingerprint.isUnavailable(mockContext));
    }

    @Test
    public void fingerprintAvailable() throws Exception {
        when(FingerprintManagerCompat.from(mockContext)).thenReturn(mockFingerprintManager);
        when(mockFingerprintManager.hasEnrolledFingerprints()).thenReturn(true);

        assertTrue("Fingerprint should be available", RxFingerprint.hasEnrolledFingerprints(mockContext));
    }

    @Test
    public void hardwareAvailable() throws Exception {
        when(FingerprintManagerCompat.from(mockContext)).thenReturn(mockFingerprintManager);
        when(mockFingerprintManager.isHardwareDetected()).thenReturn(true);

        assertTrue("Hardware should be available", RxFingerprint.isHardwareDetected(mockContext));
    }

    @Test
    public void fingerprintUnavailable() throws Exception {
        when(FingerprintManagerCompat.from(mockContext)).thenReturn(mockFingerprintManager);
        when(mockFingerprintManager.hasEnrolledFingerprints()).thenReturn(false);

        assertFalse("Fingerprint should be unavailable", RxFingerprint.hasEnrolledFingerprints(mockContext));
    }

    @Test
    public void hardwareUnavailable() throws Exception {
        when(FingerprintManagerCompat.from(mockContext)).thenReturn(mockFingerprintManager);
        when(mockFingerprintManager.isHardwareDetected()).thenReturn(false);

        assertFalse("Hardware should be available", RxFingerprint.isHardwareDetected(mockContext));
    }
}