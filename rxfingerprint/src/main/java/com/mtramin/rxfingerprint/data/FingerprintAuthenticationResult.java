/*
 * Copyright 2015 Marvin Ramin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mtramin.rxfingerprint.data;

import android.support.annotation.Nullable;

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
     *
     * Will only return a message if {@link FingerprintAuthenticationResult#result} is of type
     * {@link FingerprintResult#HELP}. <b>Returns {@code null} otherwise!</b>
     */
    @Nullable
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

    @Override
    public String toString() {
        return "FingerprintResult {"
                + "result=" + result.name() + ", "
                + "message=" + message +
                "}";
    }
}
