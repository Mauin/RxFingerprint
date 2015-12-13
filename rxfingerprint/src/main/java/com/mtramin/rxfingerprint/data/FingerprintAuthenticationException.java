package com.mtramin.rxfingerprint.data;

/**
 * Exception that gets thrown during fingerprint authentication if it fails and cannot be recovered.
 */
public class FingerprintAuthenticationException extends Throwable {
    public FingerprintAuthenticationException(CharSequence errString) {
    }
}
