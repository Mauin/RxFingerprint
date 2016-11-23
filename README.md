# RxFingerprint: Android Fingerprint Authentication and Encryption in RxJava2

RxFingerprint wraps the Android Fingerprint APIs (introduced in API 23) to authenticate your users and encrypt their data with their fingerprints!

Learn more about the Android Fingerprint APIs at <a href="https://developer.android.com/about/versions/marshmallow/android-6.0.html#fingerprint-authentication">developer.android.com</a>.

This library is compatible until minSdkVersion 15, but will only really work on API level 23. Below that it will provide no functionality due to the missing APIs.

RxFingerprint makes it easy for you to authenticate the user with just his fingerprint. For more on how to use RxFingerprint see [Usage](#usage).

Additionally it provides the possibility to encrypt/decrypt data with the users fingerprint.
This can be used to easily encrypt user passwords that can be decrypted again later when the user authenticates with their fingerprint. For more information about how to use these functions,
see [Encryption/Decryption](#encryption-and-decryption).

## Usage

To use RxFingerprint in your project, add the library as a dependency in your `build.gradle` file:
```groovy
dependencies {
    compile 'com.mtramin:rxfingerprint:2.0.1'
}
```

Furthermore, you have to declare the Fingerprint permission in your `AndroidManifest.xml`:
```xml
<uses-permission android:name="android.permission.USE_FINGERPRINT" />
```


Below you will find an overview on how to use the different functionalities of RxFingerprint.

### Authentication

To simply authenticate the user with his fingerprint, call the following:

``` java
Disposable disposable = RxFingerprint.authenticate(this)
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
                });
```

By subscribing to `RxFingerprint.authenticate(Context)` the fingerprint sensor of the device will be warmed up and wait for the user to touch it with one of his registered fingerprints.
Should the device not contain a fingerprint sensor or the user has not enrolled any fingerprints, `onError` will be called.

After successful authentication or a recoverable error (e.g. the sensor could not read the fingerprint clearly) `onNext` will be called. You can check the result if the authentication was successful.
In the case of a recoverable error it provides the error message.

By unsubscribing from the Subscription, the fingerprint sensor will be disabled again with no result.

### Encryption-and-decryption

Usage of the Encryption and decryption features of RxFingerprint are very similar to simple authentication calls. For more details about the cryptography used and it's security, see [Cryptography](#cryptography)

``` java
Disposable disposable = RxFingerprint.encrypt(this, stringToEncrypt)
                .subscribe(encryptionResult -> {
                    switch (fingerprintEncryptionResult.getResult()) {
                        case FAILED:
                            setStatusText("Fingerprint not recognized, try again!");
                            break;
                        case HELP:
                            setStatusText(fingerprintEncryptionResult.getMessage());
                            break;
                        case AUTHENTICATED:
                            String encrypted = fingerprintEncryptionResult.getEncrypted();
                            // Do something with encrypted data
                            break;
                    }
                }, throwable -> {
                    Log.e("ERROR", "encrypt", throwable);
                });
```

`RxFingerprint.encrypt(Context, String)` takes the String you want to encrypt (which might be a token, user password, or any other String) and an optional key name.
The given String will be encrypted with a key in the Android KeyStore and returns an encrypted String. The key used in the encryption is only accessible from your own app.
Store the encrypted String anywhere and use it later to decrypt the original value by calling:

``` java
Disposable disposable = RxFingerprint.decrypt(this, encryptedString)
                .subscribe(decryptionResult -> {
                    switch (fingerprintDecryptionResult.getResult()) {
                        case FAILED:
                            setStatusText("Fingerprint not recognized, try again!");
                            break;
                        case HELP:
                            setStatusText(fingerprintDecryptionResult.getMessage());
                            break;
                        case AUTHENTICATED:
                            String decrypted = fingerprintDecryptionResult.getDecrypted();
                            // Do something with decrypted data
                            break;
                    }
                }, throwable -> {
                    if (RxFingerprint.keyInvalidated(throwable)) {
                        // The keys you wanted to use are invalidated because the user has turned off his
                        // secure lock screen or changed the fingerprints stored on the device
                        // You have to re-encrypt the data to access it
                    }
                    Log.e("ERROR", "decrypt", throwable);
                });
```

Be aware that all encryption keys will be invalidated once the user changes his lockscreen or changes his enrolled fingerprints. If you receive an `onError` event
during decryption check if the keys were invalidated with `RxFingerprint.keyInvalidated(Throwable)` and prompt the user to authenticate and encrypt his data again.

### Best-practices

To prevent errors and ensure a good user experience, make sure to think of these cases:

- Before calling any RxFingerprint authentication, check if the user can use fingerprint authentication by calling: `RxFingerprint.isAvailable(Context)` or `RxFingerprint.isUnavailable(Context)`
- Always check for recoverable errors in any `onNext` events and provide the user with the given Error message in the result.

## Cryptography

Encryption and Decryption in RxFingerprint is backed by the [Android KeyStore System](https://developer.android.com/training/articles/keystore.html).
If you do not provide a key name in the encryption and decryption calls, a default key will be generated from the package name of your application.

The encryption relies on the [Advanced Encryption Standard](https://en.wikipedia.org/wiki/Advanced_Encryption_Standard) with a 256-bit keysize.

After the encryption step all results will be Base64 encoded for easier transportation and storage.

## Dependencies

RxFingerprint brings the following dependencies:

- RxJava2
- AppCompat-v7 to allow for backwards compability (which will just do nothing)

## Bugs and Feedback

For bugs, questions and discussions please use the [Github Issues](https://github.com/mauin/RxFingerprint/issues).
 
## LICENSE

Copyright 2015 Marvin Ramin.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

<http://www.apache.org/licenses/LICENSE-2.0>

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
