package com.mtramin.rxfingerprint.data;


/**
 * TODO: JAVADOC
 */
public class FingerprintDecryptionResult extends FingerprintAuthenticationResult {

    private String decrypted;

    public FingerprintDecryptionResult(FingerprintResult result, String message, String decrypted) {
        super(result, message);
        this.decrypted = decrypted;
    }

    public String getDecrypted() {
        if (!isSuccess()) {
            throw new IllegalAccessError("Fingerprint authentication was not successful, cannot access decryption result");
        }
        return decrypted;
    }
}