package com.mtramin.rxfingerprint.data;


/**
 * Result of a fingerprint authenticated encryption operation
 */
public class FingerprintEncryptionResult extends FingerprintAuthenticationResult {

    private String encrypted;

    /**
     * Default constructor
     *
     * @param result    result of the operation
     * @param message   message to be displayed to the user
     * @param encrypted encrypted data
     */
    public FingerprintEncryptionResult(FingerprintResult result, String message, String encrypted) {
        super(result, message);
        this.encrypted = encrypted;
    }

    /**
     * @return encrypted data, can only be accessed if the result was of
     * type {@link FingerprintResult#AUTHENTICATED}
     */
    public String getEncrypted() {
        if (!isSuccess()) {
            throw new IllegalAccessError("Fingerprint authentication was not successful, cannot access encryption result");
        }
        return encrypted;
    }
}
