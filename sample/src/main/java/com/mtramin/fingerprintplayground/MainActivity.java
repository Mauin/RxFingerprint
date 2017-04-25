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

package com.mtramin.fingerprintplayground;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.mtramin.rxfingerprint.EncryptionMethod;
import com.mtramin.rxfingerprint.RxFingerprint;

import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;

/**
 * Shows example usage of RxFingerprint
 */
public class MainActivity extends AppCompatActivity {

    private TextView statusText;
    private EditText input;
    private ViewGroup layout;
    private int key;

    private Disposable fingerprintDisposable = Disposables.empty();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.statusText = (TextView) findViewById(R.id.status);

        findViewById(R.id.authenticate).setOnClickListener(v -> authenticate());
        findViewById(R.id.encrypt).setOnClickListener(v -> encrypt());

        input = (EditText) findViewById(R.id.input);
        layout = (ViewGroup) findViewById(R.id.layout);
    }

    @Override
    protected void onStop() {
        super.onStop();

        fingerprintDisposable.dispose();
    }

    private void setStatusText(String text) {
        statusText.setText(text);
    }

    private void setStatusText() {
        if (!RxFingerprint.isAvailable(this)) {
            setStatusText("Fingerprint not available");
            return;
        }

        setStatusText("Touch the sensor!");
    }

    private void authenticate() {
        setStatusText();

        if (RxFingerprint.isUnavailable(this)) {
            return;
        }

        fingerprintDisposable = RxFingerprint.authenticate(this)
                .subscribe(fingerprintAuthenticationResult -> {
                    switch (fingerprintAuthenticationResult.getResult()) {
                        case FAILED:
                            setStatusText("Fingerprint not recognized, try again!");
                            break;
                        case HELP:
                            setStatusText(fingerprintAuthenticationResult.getMessage());
                            break;
                        case AUTHENTICATED:
                            setStatusText("Successfully authenticated!");
                            break;
                    }
                }, throwable -> {
                    Log.e("ERROR", "authenticate", throwable);
                    setStatusText(throwable.getMessage());
                });
    }

    private void encrypt() {
        setStatusText();

        if (RxFingerprint.isUnavailable(this)) {
            setStatusText("RxFingerprint unavailable");
            return;
        }

        String toEncrypt = input.getText().toString();
        if (TextUtils.isEmpty(toEncrypt)) {
            setStatusText("Please enter a text to encrypt first");
            return;
        }

        fingerprintDisposable = RxFingerprint.encrypt(EncryptionMethod.RSA, this, String.valueOf(key), toEncrypt)
                .subscribe(fingerprintEncryptionResult -> {
                    switch (fingerprintEncryptionResult.getResult()) {
                        case FAILED:
                            setStatusText("Fingerprint not recognized, try again!");
                            break;
                        case HELP:
                            setStatusText(fingerprintEncryptionResult.getMessage());
                            break;
                        case AUTHENTICATED:
                            String encrypted = fingerprintEncryptionResult.getEncrypted();
                            setStatusText("encryption successful");
                            createDecryptionButton(encrypted);
                            key++;
                            break;
                    }
                }, throwable -> {
                    //noinspection StatementWithEmptyBody
                    if (RxFingerprint.keyInvalidated(throwable)) {
                        // The keys you wanted to use are invalidated because the user has turned off his
                        // secure lock screen or changed the fingerprints stored on the device
                        // You have to re-encrypt the data to access it
                    }
                    Log.e("ERROR", "encrypt", throwable);
                    setStatusText(throwable.getMessage());
                });
    }

    private void decrypt(String key, String encrypted) {
        setStatusText();

        if (!RxFingerprint.isAvailable(this)) {
            return;
        }

        fingerprintDisposable = RxFingerprint.decrypt(EncryptionMethod.RSA, this, key, encrypted)
                .subscribe(fingerprintDecryptionResult -> {
                    switch (fingerprintDecryptionResult.getResult()) {
                        case FAILED:
                            setStatusText("Fingerprint not recognized, try again!");
                            break;
                        case HELP:
                            setStatusText(fingerprintDecryptionResult.getMessage());
                            break;
                        case AUTHENTICATED:
                            setStatusText("decrypted:\n" + fingerprintDecryptionResult.getDecrypted());
                            break;
                    }
                }, throwable -> {
                    //noinspection StatementWithEmptyBody
                    if (RxFingerprint.keyInvalidated(throwable)) {
                        // The keys you wanted to use are invalidated because the user has turned off his
                        // secure lock screen or changed the fingerprints stored on the device
                        // You have to re-encrypt the data to access it
                    }
                    Log.e("ERROR", "decrypt", throwable);
                    setStatusText(throwable.getMessage());
                });
    }

    private void createDecryptionButton(final String encrypted) {
        Button button = new Button(this);
        button.setText(String.format("decrypt %d", key));
        button.setTag(new EncryptedData(key, encrypted));
        button.setOnClickListener(v -> {
            EncryptedData encryptedData = (EncryptedData) v.getTag();
            decrypt(encryptedData.key, encryptedData.encrypted);
        });
        layout.addView(button);
    }

    private static class EncryptedData {
        final String key;
        final String encrypted;

        EncryptedData(int key, String encrypted) {
            this.key = String.valueOf(key);
            this.encrypted = encrypted;
        }
    }

}
