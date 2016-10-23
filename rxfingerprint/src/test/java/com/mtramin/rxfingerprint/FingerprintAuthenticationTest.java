package com.mtramin.rxfingerprint;

import android.content.Context;
import android.os.Handler;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.support.v4.os.CancellationSignal;

import com.mtramin.rxfingerprint.data.FingerprintAuthenticationException;
import com.mtramin.rxfingerprint.data.FingerprintAuthenticationResult;
import com.mtramin.rxfingerprint.data.FingerprintResult;

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
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Tests for the Fingerprint authentication observable
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({RxFingerprint.class, FingerprintManagerCompat.class})
public class FingerprintAuthenticationTest {

    private static final String ERROR_MESSAGE = "Error message";
    private static final CharSequence MESSAGE_HELP = "Help message";

    TestSubscriber<FingerprintAuthenticationResult> testSubscriber = TestSubscriber.create();

    @Mock
    Context mockContext;

    @Mock
    FingerprintManagerCompat mockFingerprintManager;

    @Before
    public void setUp() throws Exception {
        initMocks(this);

        mockStatic(RxFingerprint.class);
        mockStatic(FingerprintManagerCompat.class);

        when(mockContext.getApplicationContext()).thenReturn(mockContext);
        when(FingerprintManagerCompat.from(mockContext)).thenReturn(mockFingerprintManager);
        when(RxFingerprint.isAvailable(mockContext)).thenReturn(true);
    }

    @Test
    public void testFingerprintNotAvailable() throws Exception {
        when(RxFingerprint.isAvailable(mockContext)).thenReturn(false);
        FingerprintAuthenticationObservable.create(mockContext).subscribe(testSubscriber);

        testSubscriber.awaitTerminalEvent();
        testSubscriber.assertNoValues();
        testSubscriber.assertError(IllegalAccessException.class);
    }


    @Test
    public void testAuthenticationSuccessful() throws Exception {
        FingerprintAuthenticationObservable.create(mockContext).subscribe(testSubscriber);

        ArgumentCaptor<FingerprintManagerCompat.AuthenticationCallback> callbackCaptor = ArgumentCaptor.forClass(FingerprintManagerCompat.AuthenticationCallback.class);
        verify(mockFingerprintManager).authenticate(any(FingerprintManagerCompat.CryptoObject.class), anyInt(), any(CancellationSignal.class), callbackCaptor.capture(), any(Handler.class));
        callbackCaptor.getValue().onAuthenticationSucceeded(new FingerprintManagerCompat.AuthenticationResult(null));

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

        ArgumentCaptor<FingerprintManagerCompat.AuthenticationCallback> callbackCaptor = ArgumentCaptor.forClass(FingerprintManagerCompat.AuthenticationCallback.class);
        verify(mockFingerprintManager).authenticate(any(FingerprintManagerCompat.CryptoObject.class), anyInt(), any(CancellationSignal.class), callbackCaptor.capture(), any(Handler.class));
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

        ArgumentCaptor<FingerprintManagerCompat.AuthenticationCallback> callbackCaptor = ArgumentCaptor.forClass(FingerprintManagerCompat.AuthenticationCallback.class);
        verify(mockFingerprintManager).authenticate(any(FingerprintManagerCompat.CryptoObject.class), anyInt(), any(CancellationSignal.class), callbackCaptor.capture(), any(Handler.class));
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

        ArgumentCaptor<FingerprintManagerCompat.AuthenticationCallback> callbackCaptor = ArgumentCaptor.forClass(FingerprintManagerCompat.AuthenticationCallback.class);
        verify(mockFingerprintManager).authenticate(any(FingerprintManagerCompat.CryptoObject.class), anyInt(), any(CancellationSignal.class), callbackCaptor.capture(), any(Handler.class));
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

        ArgumentCaptor<FingerprintManagerCompat.AuthenticationCallback> callbackCaptor = ArgumentCaptor.forClass(FingerprintManagerCompat.AuthenticationCallback.class);
        verify(mockFingerprintManager).authenticate(any(FingerprintManagerCompat.CryptoObject.class), anyInt(), any(CancellationSignal.class), callbackCaptor.capture(), any(Handler.class));
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

        callbackCaptor.getValue().onAuthenticationSucceeded(new FingerprintManagerCompat.AuthenticationResult(null));

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
