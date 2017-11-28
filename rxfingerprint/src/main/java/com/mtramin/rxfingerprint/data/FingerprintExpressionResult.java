package com.mtramin.rxfingerprint.data;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Result of a fingerprint request.
 */
public abstract class FingerprintExpressionResult {
    /**
     * @return message that can be displayed to the user to help him guide through the
     * authentication process
     * <p>
     * Will only return a message if {@link FingerprintExpressionResult#getResult()} is of type
     * {@link FingerprintResult#HELP}. <b>Returns {@code null} otherwise!</b>
     */
    @Nullable
    public abstract String getMessage();

    /**
     * @return result of fingerprint authentication operation
     */
    @NonNull
    public abstract FingerprintResult getResult();

    /**
     * @return {@code true} if authentication was successful
     */
    public final boolean isSuccess() {
        return getResult() == FingerprintResult.AUTHENTICATED;
    }
}
