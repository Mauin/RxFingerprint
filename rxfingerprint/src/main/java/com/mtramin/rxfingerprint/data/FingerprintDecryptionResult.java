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


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;

/**
 * Result of a decryption operation with fingerprint authentication.
 */
@AutoValue
public abstract class FingerprintDecryptionResult extends FingerprintExpressionResult {
    /**
     * Default constructor
     *
     * @param result    result of the fingerprint authentication
     * @param message   message to be displayed to the user
     * @param decrypted decrypted data
     */
    public static FingerprintDecryptionResult create(FingerprintResult result, String message, String decrypted) {
        return new AutoValue_FingerprintDecryptionResult(message, result, decrypted);
    }

    @Redacted
    @Nullable
    abstract String decryptedValue();

    /**
     * @return decrypted data as a String. Can only be accessed if the result of the fingerprint
     * authentication was of type {@link FingerprintResult#AUTHENTICATED}.
     */
    @NonNull
    public final String getDecrypted() {
        if (!isSuccess()) {
            throw new IllegalAccessError("Fingerprint authentication was not successful, cannot access decryption result");
        }
        //noinspection ConstantConditions Is non-null if the expression is successful.
        return decryptedValue();
    }
}