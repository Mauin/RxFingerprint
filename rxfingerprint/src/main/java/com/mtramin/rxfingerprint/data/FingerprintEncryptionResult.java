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


/**
 * Result of a fingerprint authenticated encryption operation
 */
public class FingerprintEncryptionResult extends FingerprintAuthenticationResult {

    private final String encrypted;

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
