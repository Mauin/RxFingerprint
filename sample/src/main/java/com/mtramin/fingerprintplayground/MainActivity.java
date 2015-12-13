package com.mtramin.fingerprintplayground;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.mtramin.rxfingerprint.RxFingerprint;
import com.mtramin.rxfingerprint.data.FingerprintAuthenticationResult;
import com.mtramin.rxfingerprint.data.FingerprintDecryptionResult;
import com.mtramin.rxfingerprint.data.FingerprintEncryptionResult;

import rx.Subscription;
import rx.functions.Action1;
import rx.observers.Subscribers;

/**
 * Shows example usage of RxFingerprint
 */
public class MainActivity extends AppCompatActivity {

    public static final String SAMPLE_KEY = "RxFingerprint_Key";

    private TextView statusText;
    private EditText input;
    private ViewGroup layout;

    private Subscription fingerprintSubscription = Subscribers.empty();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.statusText = (TextView) findViewById(R.id.status);

        findViewById(R.id.authenticate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                authenticate();
            }
        });

        findViewById(R.id.encrypt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                encrypt();
            }
        });
        input = (EditText) findViewById(R.id.input);
        layout = (ViewGroup) findViewById(R.id.layout);
    }

    @Override
    protected void onStop() {
        super.onStop();

        fingerprintSubscription.unsubscribe();
    }

    private void setStatusText(String text) {
        statusText.setText(text);
    }

    private void preFingerprintAction() {
        if (!RxFingerprint.isAvailable(this)) {
            setStatusText("Fingerprint not available");
            return;
        }

        setStatusText("Touch the sensor!");
    }

    private void authenticate() {
        preFingerprintAction();

        fingerprintSubscription = RxFingerprint.authenticate(this)
                .subscribe(new Action1<FingerprintAuthenticationResult>() {
                    @Override
                    public void call(FingerprintAuthenticationResult fingerprintAuthenticationResult) {
                        if (fingerprintAuthenticationResult.isSuccess()) {
                            setStatusText("Successfully authenticated!");
                        } else {
                            setStatusText(fingerprintAuthenticationResult.getMessage());
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e("ERROR", "authenticate", throwable);
                    }
                });
    }

    private void encrypt() {
        preFingerprintAction();

        String toEncrypt = input.getText().toString();
        if (TextUtils.isEmpty(toEncrypt)) {
            setStatusText("Please enter a text to encrypt first");
            return;
        }

        fingerprintSubscription = RxFingerprint.encrypt(this, SAMPLE_KEY, toEncrypt)
                .subscribe(new Action1<FingerprintEncryptionResult>() {
                    @Override
                    public void call(FingerprintEncryptionResult fingerprintEncryptionResult) {
                        if (fingerprintEncryptionResult.isSuccess()) {
                            String encrypted = fingerprintEncryptionResult.getEncrypted();
                            setStatusText("encryption successful");
                            createDecryptionButton(encrypted);
                        } else {
                            setStatusText(fingerprintEncryptionResult.getMessage());
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        if (RxFingerprint.keyInvalidated(throwable)) {
                            // The keys you wanted to use are invalidated because the user has turned off his
                            // secure lock screen or changed the fingerprints stored on the device
                            // You have to re-encrypt the data to access it
                        }
                        Log.e("ERROR", "encrypt", throwable);
                    }
                });
    }

    private void decrypt(String encrypted) {
        preFingerprintAction();

        fingerprintSubscription = RxFingerprint.decrypt(this, SAMPLE_KEY, encrypted)
                .subscribe(new Action1<FingerprintDecryptionResult>() {
                    @Override
                    public void call(FingerprintDecryptionResult fingerprintDecryptionResult) {
                        if (fingerprintDecryptionResult.isSuccess()) {
                            setStatusText("decrypted:\n" + fingerprintDecryptionResult.getDecrypted());
                        } else {
                            setStatusText(fingerprintDecryptionResult.getMessage());
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        if (RxFingerprint.keyInvalidated(throwable)) {
                            // The keys you wanted to use are invalidated because the user has turned off his
                            // secure lock screen or changed the fingerprints stored on the device
                            // You have to re-encrypt the data to access it
                        }
                        Log.e("ERROR", "decrypt", throwable);
                    }
                });
    }

    private void createDecryptionButton(final String encrypted) {
        Button button = new Button(this);
        button.setText("decrypt " + encrypted.substring(0, 6) + "...");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                decrypt(encrypted);
            }
        });
        layout.addView(button);
    }

}
