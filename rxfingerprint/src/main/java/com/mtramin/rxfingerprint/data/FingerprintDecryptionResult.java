package com.mtramin.rxfingerprint.data;


/**
 * Result of a decryption operation with fingerprint authentication.
 */
public class FingerprintDecryptionResult extends FingerprintAuthenticationResult {

    private final String decrypted;

    /**
     * Default constructor
     *
     * @param result    result of the fingerprint authentication
     * @param message   message to be displayed to the user
     * @param decrypted decrypted data
     */
    public FingerprintDecryptionResult(FingerprintResult result, String message, String decrypted) {
        super(result, message);
        this.decrypted = decrypted;
    }

    /**
     * @return decrypted data as a String. Can only be accessed if the result of the fingerprint
     * authentication was of type {@link FingerprintResult#AUTHENTICATED}.
     */
    public String getDecrypted() {
        if (!isSuccess()) {
            throw new IllegalAccessError("Fingerprint authentication was not successful, cannot access decryption result");
        }
        return decrypted;
    }
}