package com.mtramin.fingerprintplayground;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.mtramin.rxfingerprint.RxFingerprint;
import com.mtramin.rxfingerprint.data.FingerprintAuthenticationResult;
import com.mtramin.rxfingerprint.data.FingerprintDecryptionResult;
import com.mtramin.rxfingerprint.data.FingerprintEncryptionResult;

import rx.Subscription;
import rx.functions.Action1;
import rx.observers.Subscribers;

public class MainActivity extends AppCompatActivity {

    private TextView statusText;
    private String encrypted = null;

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

        findViewById(R.id.decrypt).setEnabled(false);
        findViewById(R.id.decrypt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                decrypt();
            }
        });
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
        if (!RxFingerprint.available(this)) {
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

        fingerprintSubscription = RxFingerprint.encrypt(this, "1234")
                .subscribe(new Action1<FingerprintEncryptionResult>() {
                    @Override
                    public void call(FingerprintEncryptionResult fingerprintEncryptionResult) {
                        if (fingerprintEncryptionResult.isSuccess()) {
                            encrypted = fingerprintEncryptionResult.getEncrypted();
                            setStatusText("'1234' encrypted is:\n" + encrypted);
                            findViewById(R.id.decrypt).setEnabled(true);
                        } else {
                            setStatusText("Something went wrong during encryption!");
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e("ERROR", "encrypt", throwable);
                    }
                });
    }

    private void decrypt() {
        preFingerprintAction();

        fingerprintSubscription = RxFingerprint.decrypt(this, encrypted)
                .subscribe(new Action1<FingerprintDecryptionResult>() {
                    @Override
                    public void call(FingerprintDecryptionResult fingerprintDecryptionResult) {
                        if (fingerprintDecryptionResult.isSuccess()) {
                            setStatusText("Previously encrypted '1234' decrypted:\n" + fingerprintDecryptionResult.getDecrypted());
                        } else {
                            setStatusText("Something went wrong during decryption!");
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e("ERROR", "decrypt", throwable);
                    }
                });
    }
}
