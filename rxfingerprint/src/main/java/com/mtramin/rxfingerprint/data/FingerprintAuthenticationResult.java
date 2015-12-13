package com.mtramin.rxfingerprint.data;


/**
 * Result of a fingerprint based authentication.
 */
public class FingerprintAuthenticationResult {
    private final FingerprintResult result;
    private final String message;

    /**
     * Default constructor
     *
     * @param result  result of the fingerprint authentication
     * @param message optional message to be displayed to the user
     */
    public FingerprintAuthenticationResult(FingerprintResult result, String message) {
        this.result = result;
        this.message = message;
    }

    /**
     * @return message that can be displayed to the user to help him guide through the
     * authentication process
     */
    public String getMessage() {
        return message;
    }

    /**
     * @return result of fingerprint authentication operation
     */
    public FingerprintResult getResult() {
        return result;
    }

    /**
     * @return {@code true} if authentication was successful
     */
    public boolean isSuccess() {
        return result == FingerprintResult.AUTHENTICATED;
    }
}
