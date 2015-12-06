package com.mtramin.rxfingerprint.data;


/**
 * TODO: JAVADOC
 */
public class FingerprintAuthenticationResult {
    private FingerprintResult result;
    private String message;


    public FingerprintAuthenticationResult(FingerprintResult result, String message) {
        this.result = result;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public FingerprintResult getResult() {
        return result;
    }

    public boolean isSuccess() {
        return result == FingerprintResult.AUTHENTICATED;
    }
}
