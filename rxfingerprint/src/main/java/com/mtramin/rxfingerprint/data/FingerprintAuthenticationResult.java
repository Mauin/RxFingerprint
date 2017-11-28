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
 * Result of a fingerprint based authentication.
 */
@AutoValue
public abstract class FingerprintAuthenticationResult extends FingerprintExpressionResult {
    /**
     * Creates a new instance of {@linkplain FingerprintAuthenticationResult}.
     *
     * @param result  result of the fingerprint authentication
     * @param message optional message to be displayed to the user
     */
    public static FingerprintAuthenticationResult create(@NonNull FingerprintResult result, @Nullable String message) {
        return new AutoValue_FingerprintAuthenticationResult(message, result);
    }
}
