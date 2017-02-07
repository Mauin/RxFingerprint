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

import rx.observers.TestSubscriber;

import static org.junit.Assert.assertFalse;
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
@PrepareForTest({RxFingerprint.class, FingerprintApiProvider.class, FingerprintManager.class})
public class FingerprintAuthenticationTest {

    private static final String ERROR_MESSAGE = "Error message";
    private static final CharSequence MESSAGE_HELP = "Help message";

    TestSubscriber<FingerprintAuthenticationResult> testSubscriber = TestSubscriber.create();

    @Mock
    Context mockContext;

    @Mock
    FingerprintManager mockFingerprintManager;

    @Before
    public void setUp() throws Exception {
        initMocks(this);

        mockStatic(RxFingerprint.class);
        mockStatic(FingerprintApiProvider.class);
        mockStatic(FingerprintManager.class);

        when(mockContext.getApplicationContext()).thenReturn(mockContext);
        when(mockContext.getSystemService(Context.FINGERPRINT_SERVICE)).thenReturn(mockFingerprintManager);
        when(FingerprintApiProvider.getFingerprintManager(mockContext)).thenReturn(mockFingerprintManager);
        when(FingerprintApiProvider.createCancellationSignal()).thenReturn(mock(CancellationSignal.class));
		when(RxFingerprint.isUnavailable(mockContext)).thenReturn(false);
    }

    @Test
    public void testFingerprintNotAvailable() throws Exception {
        when(RxFingerprint.isUnavailable(mockContext)).thenReturn(true);
        FingerprintAuthenticationObservable.create(mockContext).subscribe(testSubscriber);

        testSubscriber.awaitTerminalEvent();
        testSubscriber.assertNoValues();
        testSubscriber.assertError(FingerprintUnavailableException.class);
    }

    @Test
    public void testAuthenticationSuccessful() throws Exception {
        FingerprintAuthenticationObservable.create(mockContext).subscribe(testSubscriber);
        AuthenticationResult result = mock(AuthenticationResult.class);

        ArgumentCaptor<FingerprintManager.AuthenticationCallback> callbackCaptor = ArgumentCaptor.forClass(FingerprintManager.AuthenticationCallback.class);
        verify(mockFingerprintManager).authenticate(any(CryptoObject.class), any(CancellationSignal.class), anyInt(), callbackCaptor.capture(), any(Handler.class));
        callbackCaptor.getValue().onAuthenticationSucceeded(result);

        testSubscriber.awaitTerminalEvent();
        testSubscriber.assertNoErrors();
        testSubscriber.assertCompleted();
        testSubscriber.assertUnsubscribed();
        testSubscriber.assertValueCount(1);

        FingerprintAuthenticationResult fingerprintAuthenticationResult = testSubscriber.getOnNextEvents().get(0);
        assertTrue("Authentication should be successful", fingerprintAuthenticationResult.isSuccess());
        assertTrue("Result should be equal AUTHENTICATED", fingerprintAuthenticationResult.getResult().equals(FingerprintResult.AUTHENTICATED));
        assertTrue("Should contain no message", fingerprintAuthenticationResult.getMessage() == null);
    }

    @Test
    public void testAuthenticationError() throws Exception {
        FingerprintAuthenticationObservable.create(mockContext).subscribe(testSubscriber);

        ArgumentCaptor<FingerprintManager.AuthenticationCallback> callbackCaptor = ArgumentCaptor.forClass(FingerprintManager.AuthenticationCallback.class);
        verify(mockFingerprintManager).authenticate(any(CryptoObject.class), any(CancellationSignal.class), anyInt(), callbackCaptor.capture(), any(Handler.class));
        callbackCaptor.getValue().onAuthenticationError(0, ERROR_MESSAGE);

        testSubscriber.awaitTerminalEvent();
        testSubscriber.assertError(FingerprintAuthenticationException.class);
        testSubscriber.assertUnsubscribed();
        testSubscriber.assertValueCount(0);

        assertTrue("Should contain 1 error", testSubscriber.getOnErrorEvents().size() == 1);

        Throwable throwable = testSubscriber.getOnErrorEvents().get(0);
        assertTrue("Message should equal ERROR_MESSAGE", throwable.getMessage().equals(ERROR_MESSAGE));
    }

    @Test
    public void testAuthenticationFailed() throws Exception {
        FingerprintAuthenticationObservable.create(mockContext).subscribe(testSubscriber);

        ArgumentCaptor<FingerprintManager.AuthenticationCallback> callbackCaptor = ArgumentCaptor.forClass(FingerprintManager.AuthenticationCallback.class);
        verify(mockFingerprintManager).authenticate(any(CryptoObject.class), any(CancellationSignal.class), anyInt(), callbackCaptor.capture(), any(Handler.class));
        callbackCaptor.getValue().onAuthenticationFailed();

        testSubscriber.assertNoTerminalEvent();
        testSubscriber.assertNoErrors();
        testSubscriber.assertNotCompleted();
        testSubscriber.assertValueCount(1);
        assertFalse("Should not be unsubscribed", testSubscriber.isUnsubscribed());

        FingerprintAuthenticationResult fingerprintAuthenticationResult = testSubscriber.getOnNextEvents().get(0);
        assertTrue("Authentication should not be successful", !fingerprintAuthenticationResult.isSuccess());
        assertTrue("Result should be equal FAILED", fingerprintAuthenticationResult.getResult().equals(FingerprintResult.FAILED));
        assertTrue("Should contain no message", fingerprintAuthenticationResult.getMessage() == null);
    }

    @Test
    public void testAuthenticationHelp() throws Exception {
        FingerprintAuthenticationObservable.create(mockContext).subscribe(testSubscriber);

        ArgumentCaptor<FingerprintManager.AuthenticationCallback> callbackCaptor = ArgumentCaptor.forClass(FingerprintManager.AuthenticationCallback.class);
        verify(mockFingerprintManager).authenticate(any(CryptoObject.class), any(CancellationSignal.class), anyInt(), callbackCaptor.capture(), any(Handler.class));
        callbackCaptor.getValue().onAuthenticationHelp(0, MESSAGE_HELP);

        testSubscriber.assertNoTerminalEvent();
        testSubscriber.assertNoErrors();
        testSubscriber.assertNotCompleted();
        testSubscriber.assertValueCount(1);
        assertFalse("Should not be unsubscribed", testSubscriber.isUnsubscribed());

        FingerprintAuthenticationResult fingerprintAuthenticationResult = testSubscriber.getOnNextEvents().get(0);
        assertTrue("Authentication should not be successful", !fingerprintAuthenticationResult.isSuccess());
        assertTrue("Result should be equal HELP", fingerprintAuthenticationResult.getResult().equals(FingerprintResult.HELP));
        assertTrue("Should contain help message", fingerprintAuthenticationResult.getMessage().equals(MESSAGE_HELP));
    }

    @Test
    public void testAuthenticationSuccessfulOnSecondTry() throws Exception {
        FingerprintAuthenticationObservable.create(mockContext).subscribe(testSubscriber);

        ArgumentCaptor<FingerprintManager.AuthenticationCallback> callbackCaptor = ArgumentCaptor.forClass(FingerprintManager.AuthenticationCallback.class);
        verify(mockFingerprintManager).authenticate(any(CryptoObject.class), any(CancellationSignal.class), anyInt(), callbackCaptor.capture(), any(Handler.class));
        callbackCaptor.getValue().onAuthenticationHelp(0, MESSAGE_HELP);

        testSubscriber.assertNoTerminalEvent();
        testSubscriber.assertNoErrors();
        testSubscriber.assertNotCompleted();
        testSubscriber.assertValueCount(1);
        assertFalse("Should not be unsubscribed", testSubscriber.isUnsubscribed());

        FingerprintAuthenticationResult helpResult = testSubscriber.getOnNextEvents().get(0);
        assertTrue("Authentication should not be successful", !helpResult.isSuccess());
        assertTrue("Result should be equal HELP", helpResult.getResult().equals(FingerprintResult.HELP));
        assertTrue("Should contain help message", helpResult.getMessage().equals(MESSAGE_HELP));

        callbackCaptor.getValue().onAuthenticationSucceeded(mock(AuthenticationResult.class));

        testSubscriber.awaitTerminalEvent();
        testSubscriber.assertNoErrors();
        testSubscriber.assertCompleted();
        testSubscriber.assertUnsubscribed();
        testSubscriber.assertValueCount(2);

        FingerprintAuthenticationResult successResult = testSubscriber.getOnNextEvents().get(1);
        assertTrue("Authentication should be successful", successResult.isSuccess());
        assertTrue("Result should be equal AUTHENTICATED", successResult.getResult().equals(FingerprintResult.AUTHENTICATED));
        assertTrue("Should contain no message", successResult.getMessage() == null);

    }

}
