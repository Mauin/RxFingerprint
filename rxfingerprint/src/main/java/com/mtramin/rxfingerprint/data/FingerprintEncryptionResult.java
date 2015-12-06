package com.mtramin.rxfingerprint.data;


/**
 * TODO: JAVADOC
 */
public class FingerprintEncryptionResult extends FingerprintAuthenticationResult {

    private String encrypted;

    public FingerprintEncryptionResult(FingerprintResult result, String message, String encrypted) {
        super(result, message);
        this.encrypted = encrypted;
    }

    public String getEncrypted() {
        if (!isSuccess()) {
            throw new IllegalAccessError("Fingerprint authentication was not successful, cannot access encryption result");
        }
        return encrypted;
    }
}
