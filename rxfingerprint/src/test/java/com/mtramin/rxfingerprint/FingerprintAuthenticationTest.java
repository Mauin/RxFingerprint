package com.mtramin.rxfingerprint;

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
import org.mockito.runners.MockitoJUnitRunner;

import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for the Fingerprint authentication observable
 */
@SuppressWarnings({"NewApi", "MissingPermission"})
@RunWith(MockitoJUnitRunner.class)
public class FingerprintAuthenticationTest {

    private static final String ERROR_MESSAGE = "Error message";
    private static final CharSequence MESSAGE_HELP = "Help message";

    @Mock FingerprintApiWrapper fingerprintApiWrapper;
    @Mock FingerprintManager fingerprintManager;
    @Mock CancellationSignal cancellationSignal;

    Observable<FingerprintAuthenticationResult> observable;

    @Before
    public void setUp() throws Exception {
        when(fingerprintApiWrapper.createCancellationSignal()).thenReturn(cancellationSignal);

        observable = Observable.create(new FingerprintAuthenticationObservable(fingerprintApiWrapper));
    }

    @Test
    public void testFingerprintNotAvailable() throws Exception {
        when(fingerprintApiWrapper.isUnavailable()).thenReturn(true);

        observable.test()
                .assertNoValues()
                .assertError(FingerprintUnavailableException.class);
    }

    @Test
    public void testAuthenticationSuccessful() throws Exception {
        when(fingerprintApiWrapper.isUnavailable()).thenReturn(false);
        when(fingerprintApiWrapper.getFingerprintManager()).thenReturn(fingerprintManager);

        AuthenticationResult result = mock(AuthenticationResult.class);
        TestObserver<FingerprintAuthenticationResult> testObserver = observable.test();

        ArgumentCaptor<FingerprintManager.AuthenticationCallback> callbackCaptor = ArgumentCaptor.forClass(FingerprintManager.AuthenticationCallback.class);
        verify(fingerprintManager).authenticate(any(CryptoObject.class), any(CancellationSignal.class), anyInt(), callbackCaptor.capture(), any(Handler.class));
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
        when(fingerprintApiWrapper.isUnavailable()).thenReturn(false);
        when(fingerprintApiWrapper.getFingerprintManager()).thenReturn(fingerprintManager);

        TestObserver<FingerprintAuthenticationResult> testObserver = observable.test();

        ArgumentCaptor<FingerprintManager.AuthenticationCallback> callbackCaptor = ArgumentCaptor.forClass(FingerprintManager.AuthenticationCallback.class);
        verify(fingerprintManager).authenticate(any(CryptoObject.class), any(CancellationSignal.class), anyInt(), callbackCaptor.capture(), any(Handler.class));
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
        when(fingerprintApiWrapper.isUnavailable()).thenReturn(false);
        when(fingerprintApiWrapper.getFingerprintManager()).thenReturn(fingerprintManager);

        TestObserver<FingerprintAuthenticationResult> testObserver = observable.test();

        ArgumentCaptor<FingerprintManager.AuthenticationCallback> callbackCaptor = ArgumentCaptor.forClass(FingerprintManager.AuthenticationCallback.class);
        verify(fingerprintManager).authenticate(any(CryptoObject.class), any(CancellationSignal.class), anyInt(), callbackCaptor.capture(), any(Handler.class));
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
        when(fingerprintApiWrapper.isUnavailable()).thenReturn(false);
        when(fingerprintApiWrapper.getFingerprintManager()).thenReturn(fingerprintManager);

        TestObserver<FingerprintAuthenticationResult> testObserver = observable.test();

        ArgumentCaptor<FingerprintManager.AuthenticationCallback> callbackCaptor = ArgumentCaptor.forClass(FingerprintManager.AuthenticationCallback.class);
        verify(fingerprintManager).authenticate(any(CryptoObject.class), any(CancellationSignal.class), anyInt(), callbackCaptor.capture(), any(Handler.class));
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
        when(fingerprintApiWrapper.isUnavailable()).thenReturn(false);
        when(fingerprintApiWrapper.getFingerprintManager()).thenReturn(fingerprintManager);

        TestObserver<FingerprintAuthenticationResult> testObserver = observable.test();

        ArgumentCaptor<FingerprintManager.AuthenticationCallback> callbackCaptor = ArgumentCaptor.forClass(FingerprintManager.AuthenticationCallback.class);
        verify(fingerprintManager).authenticate(any(CryptoObject.class), any(CancellationSignal.class), anyInt(), callbackCaptor.capture(), any(Handler.class));
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

    @Test
    public void cancelsFingerprintOperationWhenDisposed() throws Exception {
        when(fingerprintApiWrapper.isUnavailable()).thenReturn(false);
        when(fingerprintApiWrapper.getFingerprintManager()).thenReturn(fingerprintManager);

        TestObserver<FingerprintAuthenticationResult> test = observable.test();
        test.dispose();

        verify(cancellationSignal).cancel();
    }
}
