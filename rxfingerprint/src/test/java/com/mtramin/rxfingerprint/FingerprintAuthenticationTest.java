package com.mtramin.rxfingerprint;

import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.hardware.fingerprint.FingerprintManager.AuthenticationResult;
import android.hardware.fingerprint.FingerprintManager.CryptoObject;
import android.os.CancellationSignal;
import android.os.Handler;

import com.mtramin.rxfingerprint.data.FingerprintAuthenticationException;
import com.mtramin.rxfingerprint.data.FingerprintAuthenticationResult;
import com.mtramin.rxfingerprint.data.FingerprintResult;
import com.mtramin.rxfingerprint.data.FingerprintUnavailableException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import io.reactivex.observers.TestObserver;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Tests for the Fingerprint authentication observable
 */
@SuppressWarnings({"NewApi", "MissingPermission"})
@RunWith(PowerMockRunner.class)
@PrepareForTest({RxFingerprint.class, FingerprintManager.class})
public class FingerprintAuthenticationTest {

    private static final String ERROR_MESSAGE = "Error message";
    private static final CharSequence MESSAGE_HELP = "Help message";

    @Mock
    Context mockContext;

    @Mock
    FingerprintManager mockFingerprintManager;

    @Before
    public void setUp() throws Exception {
        initMocks(this);

        mockStatic(RxFingerprint.class);
        mockStatic(FingerprintManager.class);

        when(mockContext.getApplicationContext()).thenReturn(mockContext);
        when(mockContext.getSystemService(Context.FINGERPRINT_SERVICE)).thenReturn(mockFingerprintManager);
        when(RxFingerprint.getFingerprintManager(mockContext)).thenReturn(mockFingerprintManager);
		when(RxFingerprint.isUnavailable(mockContext)).thenReturn(false);
    }

    @Test
    public void testFingerprintNotAvailable() throws Exception {
        when(RxFingerprint.isUnavailable(mockContext)).thenReturn(true);
        TestObserver<FingerprintAuthenticationResult> testObserver = FingerprintAuthenticationObservable.create(mockContext).test();

        testObserver.awaitTerminalEvent();
        testObserver.assertNoValues();
        testObserver.assertError(FingerprintUnavailableException.class);
    }

    @Test
    public void testAuthenticationSuccessful() throws Exception {
        AuthenticationResult result = mock(AuthenticationResult.class);
        TestObserver<FingerprintAuthenticationResult> testObserver = FingerprintAuthenticationObservable.create(mockContext).test();

        ArgumentCaptor<FingerprintManager.AuthenticationCallback> callbackCaptor = ArgumentCaptor.forClass(FingerprintManager.AuthenticationCallback.class);
        verify(mockFingerprintManager).authenticate(any(CryptoObject.class), any(CancellationSignal.class), anyInt(), callbackCaptor.capture(), any(Handler.class));
        callbackCaptor.getValue().onAuthenticationSucceeded(result);

        testObserver.awaitTerminalEvent();
        testObserver.assertNoErrors();
        testObserver.assertComplete();
        testObserver.assertValueCount(1);

        FingerprintAuthenticationResult fingerprintAuthenticationResult = testObserver.values().get(0);
        assertTrue("Authentication should be successful", fingerprintAuthenticationResult.isSuccess());
        assertTrue("Result should be equal AUTHENTICATED", fingerprintAuthenticationResult.getResult().equals(FingerprintResult.AUTHENTICATED));
        assertTrue("Should contain no message", fingerprintAuthenticationResult.getMessage() == null);
    }

    @Test
    public void testAuthenticationError() throws Exception {
        TestObserver<FingerprintAuthenticationResult> testObserver = FingerprintAuthenticationObservable.create(mockContext).test();

        ArgumentCaptor<FingerprintManager.AuthenticationCallback> callbackCaptor = ArgumentCaptor.forClass(FingerprintManager.AuthenticationCallback.class);
        verify(mockFingerprintManager).authenticate(any(CryptoObject.class), any(CancellationSignal.class), anyInt(), callbackCaptor.capture(), any(Handler.class));
        callbackCaptor.getValue().onAuthenticationError(0, ERROR_MESSAGE);

        testObserver.awaitTerminalEvent();
        testObserver.assertError(FingerprintAuthenticationException.class);
        testObserver.assertValueCount(0);

        assertTrue("Should contain 1 error", testObserver.errorCount() == 1);

        Throwable throwable = testObserver.errors().get(0);
        assertTrue("Message should equal ERROR_MESSAGE", throwable.getMessage().equals(ERROR_MESSAGE));
    }

    @Test
    public void testAuthenticationFailed() throws Exception {
        TestObserver<FingerprintAuthenticationResult> testObserver = FingerprintAuthenticationObservable.create(mockContext).test();

        ArgumentCaptor<FingerprintManager.AuthenticationCallback> callbackCaptor = ArgumentCaptor.forClass(FingerprintManager.AuthenticationCallback.class);
        verify(mockFingerprintManager).authenticate(any(CryptoObject.class), any(CancellationSignal.class), anyInt(), callbackCaptor.capture(), any(Handler.class));
        callbackCaptor.getValue().onAuthenticationFailed();

        testObserver.assertNotTerminated();
        testObserver.assertNoErrors();
        testObserver.assertNotComplete();
        testObserver.assertValueCount(1);

        FingerprintAuthenticationResult fingerprintAuthenticationResult = testObserver.values().get(0);
        assertTrue("Authentication should not be successful", !fingerprintAuthenticationResult.isSuccess());
        assertTrue("Result should be equal FAILED", fingerprintAuthenticationResult.getResult().equals(FingerprintResult.FAILED));
        assertTrue("Should contain no message", fingerprintAuthenticationResult.getMessage() == null);
    }

    @Test
    public void testAuthenticationHelp() throws Exception {
        TestObserver<FingerprintAuthenticationResult> testObserver = FingerprintAuthenticationObservable.create(mockContext).test();

        ArgumentCaptor<FingerprintManager.AuthenticationCallback> callbackCaptor = ArgumentCaptor.forClass(FingerprintManager.AuthenticationCallback.class);
        verify(mockFingerprintManager).authenticate(any(CryptoObject.class), any(CancellationSignal.class), anyInt(), callbackCaptor.capture(), any(Handler.class));
        callbackCaptor.getValue().onAuthenticationHelp(0, MESSAGE_HELP);

        testObserver.assertNotTerminated();
        testObserver.assertNoErrors();
        testObserver.assertNotComplete();
        testObserver.assertValueCount(1);

        FingerprintAuthenticationResult fingerprintAuthenticationResult = testObserver.values().get(0);
        assertTrue("Authentication should not be successful", !fingerprintAuthenticationResult.isSuccess());
        assertTrue("Result should be equal HELP", fingerprintAuthenticationResult.getResult().equals(FingerprintResult.HELP));
        assertTrue("Should contain help message", fingerprintAuthenticationResult.getMessage().equals(MESSAGE_HELP));
    }

    @Test
    public void testAuthenticationSuccessfulOnSecondTry() throws Exception {
        TestObserver<FingerprintAuthenticationResult> testObserver = FingerprintAuthenticationObservable.create(mockContext).test();

        ArgumentCaptor<FingerprintManager.AuthenticationCallback> callbackCaptor = ArgumentCaptor.forClass(FingerprintManager.AuthenticationCallback.class);
        verify(mockFingerprintManager).authenticate(any(CryptoObject.class), any(CancellationSignal.class), anyInt(), callbackCaptor.capture(), any(Handler.class));
        callbackCaptor.getValue().onAuthenticationHelp(0, MESSAGE_HELP);

        testObserver.assertNotTerminated();
        testObserver.assertNoErrors();
        testObserver.assertNotComplete();
        testObserver.assertValueCount(1);

        FingerprintAuthenticationResult helpResult = testObserver.values().get(0);
        assertTrue("Authentication should not be successful", !helpResult.isSuccess());
        assertTrue("Result should be equal HELP", helpResult.getResult().equals(FingerprintResult.HELP));
        assertTrue("Should contain help message", helpResult.getMessage().equals(MESSAGE_HELP));

        callbackCaptor.getValue().onAuthenticationSucceeded(mock(AuthenticationResult.class));

        testObserver.awaitTerminalEvent();
        testObserver.assertNoErrors();
        testObserver.assertComplete();
        testObserver.assertValueCount(2);

        FingerprintAuthenticationResult successResult = testObserver.values().get(1);
        assertTrue("Authentication should be successful", successResult.isSuccess());
        assertTrue("Result should be equal AUTHENTICATED", successResult.getResult().equals(FingerprintResult.AUTHENTICATED));
        assertTrue("Should contain no message", successResult.getMessage() == null);

    }

}
