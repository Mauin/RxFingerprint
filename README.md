# RxFingerprint: Android Fingerprint Authentication and Encryption in RxJava2

RxFingerprint wraps the Android Fingerprint APIs (introduced in Android Marshmallow) and makes it easy to:
- Authenticate your users with their fingerprint
- Encrypt and decrypt their data with fingerprint authentication

Learn more about the Android Fingerprint APIs at <a href="https://developer.android.com/about/versions/marshmallow/android-6.0.html#fingerprint-authentication">developer.android.com</a>.

This library has a minSdkVersion of `15`, but will only really work on API level `23`. Below that it will provide no functionality due to the missing APIs.

## Usage

To use RxFingerprint in your project, add the library as a dependency in your `build.gradle` file:
```groovy
dependencies {
    compile 'com.mtramin:rxfingerprint:2.2.1'
}
```

Furthermore, you have to declare the Fingerprint permission in your `AndroidManifest.xml`:
```xml
<uses-permission android:name="android.permission.USE_FINGERPRINT" />
```

### Checking for availability

Before using any fingerprint related operations it should be verified that `RxFingerprint` can be used by calling:

``` java
if (RxFingerprint.isAvailable(this)) {
    // proceed with fingerprint operation
} else {
    // fingerprint is not available
}
```

Reasons for `RxFingerprint` to report that it is not available include:
- The current device doesn't have a fingerprint sensor
- The user is not using the fingerprint sensor of the device
- The device is running an Android version that doesn't support the Android Fingerprint APIs

### Authenticating a user with their fingerprint

To authenticate the user with their fingerprint, call the following:

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

By subscribing to `RxFingerprint.authenticate(Context)` the fingerprint sensor of the device will be activated and it will wait for the user to touch it with one of their registered fingerprints.
Should the device not contain a fingerprint sensor or the user has not enrolled any fingerprints, `onError` will be called.

After successful authentication or a recoverable error (e.g. the sensor could not read the fingerprint clearly) `onNext` will be called. You should check the result to see if the authentication was successful.
In the case of a recoverable error the value provded to `onNext` contains a helpful message that can be shown to the user and the user can try again.

By disposing the `Disposable`, the fingerprint sensor will be disabled again with no result.

### Encryption-and-decryption

Usage of the Encryption and decryption features of RxFingerprint are very similar to simple authentication calls.

`RxFingerprint` supports encryption with both the AES and RSA encryption standards. They differ in the way the user needs to interact with their fingerprint sensor.
For encryption and decryption the same `EncryptionMethod` should be used. Otherwise the encrypted data cannot be decrypted.

Encryption and Decryption in RxFingerprint is backed by the [Android KeyStore System](https://developer.android.com/training/articles/keystore.html).

After the encryption step all results will be Base64 encoded for easier transportation and storage.
 
#### AES

When choosing AES for encryption and decryption the user will have to approve both actions by authentication with their fingerprint by touching the fingerprint sensor.
The encryption then relies on the [Advanced Encryption Standard](https://en.wikipedia.org/wiki/Advanced_Encryption_Standard) with a 256-bit keysize.

The usage flow for AES is as follows:
- Call `RxFingerprint.encrypt(EncryptionMethod.AES, ...)` to initialize the encryption flow
- User authenticates by touching the fingerprint sensor
- Store the encrypted data returned in the `onNext` callback

- Call `RxFingerprint.decrypt(EncryptionMethod.AES, ...)` to initialize the decryption flow
- User authenticates by touching the fingerprint sensor
- Receive the decrypted data in the `onNext` callback

#### RSA

[RSA](https://en.wikipedia.org/wiki/RSA_(cryptosystem)) encryption allows you to encrypt a value without any user action. The data to encrypt can be encrypted and a user won't need to authenticate oneself by touching the fingerprint sensor.
The encrypted data can only be decrypted again when the user authenticates by using the fingerprint sensor on their device.


The usage flow for AES is as follows:
- Call `RxFingerprint.encrypt(EncryptionMethod.RSA, ...)` to initialize the encryption flow
- Store the encrypted data returned in the `onNext` callback

- Call `RxFingerprint.decrypt(EncryptionMethod.RSA, ...)` to initialize the decryption flow
- User authenticates by touching the fingerprint sensor
- Receive the decrypted data in the `onNext` callback


#### Encrypting and decrypting values

``` java
Disposable disposable = RxFingerprint.encrypt(EncryptionMethod.RSA, this, keyName, stringToEncrypt)
                   .subscribe(encryptionResult -> {
                       switch (encryptionResult.getResult()) {
                           case FAILED:
                               setStatusText("Fingerprint not recognized, try again!");
                               break;
                           case HELP:
                               setStatusText(encryptionResult.getMessage());
                               break;
                           case AUTHENTICATED:
                               setStatusText("Successfully authenticated!");
                               break;
                       }
                   }, throwable -> {
                       Log.e("ERROR", "authenticate", throwable);
                       setStatusText(throwable.getMessage());
                   });
```

`RxFingerprint.encrypt(EncryptionMethod, Context, String, String)` takes the String you want to encrypt (which might be a token, user password, or any other String) and a key name.
The given String will be encrypted with a key in the Android KeyStore and returns an encrypted String. The key used in the encryption is only accessible from your own app.
Store the encrypted String anywhere and use it later to decrypt the original value by calling:

``` java
Disposable disposable = RxFingerprint.decrypt(EncryptionMethod.RSA, this, keyName, encryptedValue)
                    .subscribe(decryptionResult -> {
                        switch (decryptionResult.getResult()) {
                            case FAILED:
                                setStatusText("Fingerprint not recognized, try again!");
                                break;
                            case HELP:
                                setStatusText(decryptionResult.getMessage());
                                break;
                            case AUTHENTICATED:
                                setStatusText("decrypted:\n" + decryptionResult.getDecrypted());
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
```

Be aware that all encryption keys will be invalidated once the user changes their lockscreen or changes any of their enrolled fingerprints. If you receive an `onError` event
during decryption check if the keys were invalidated with `RxFingerprint.keyInvalidated(Throwable)` and prompt the user to encrypt their data again.

Once the encryption keys are invalidated RxFingerprint will delete and renew the keys in the Android Keystore on the next call to `RxFingerprint.encrypt(...)`. 

### Best-practices

To prevent errors and ensure a good user experience, make sure to think of these cases:

- Before calling any RxFingerprint authentication, check if the user can use fingerprint authentication by calling: `RxFingerprint.isAvailable(Context)` or `RxFingerprint.isUnavailable(Context)`
- Always check for recoverable errors in any `onNext` events and provide the user with the given Error message in the result.
- If keys were invalidated due to the user changing their lockscreen or enrolled fingerprints provide them with a way to encrypt their data again.

## Dependencies

RxFingerprint brings the following dependencies:

- RxJava2
- Android Support Annotations

## Bugs and Feedback

For bugs, questions and discussions please use the [Github Issues](https://github.com/mauin/RxFingerprint/issues).
 
## LICENSE

Copyright 2015-2017 Marvin Ramin.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

<http://www.apache.org/licenses/LICENSE-2.0>

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
