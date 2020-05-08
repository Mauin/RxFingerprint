/*
 * Copyright 2017 Marvin Ramin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * <http://www.apache.org/licenses/LICENSE-2.0>
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mtramin.rxfingerprint;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.biometrics.BiometricManager;
import androidx.core.hardware.fingerprint.FingerprintManagerCompat;
import androidx.core.hardware.fingerprint.FingerprintManagerCompat.CryptoObject;
import androidx.core.hardware.fingerprint.FingerprintManagerCompat.AuthenticationCallback;
import android.os.Build;
import android.util.Log;

import java.lang.ref.WeakReference;

import androidx.core.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.biometric.BiometricPrompt;
import androidx.fragment.app.FragmentActivity;
import io.reactivex.ObservableEmitter;

import static android.Manifest.permission.USE_BIOMETRIC;
import static android.Manifest.permission.USE_FINGERPRINT;

@SuppressLint("NewApi")
@SuppressWarnings("MissingPermission")
class FingerprintApiWrapper {

	@NonNull private final Context context;
	@Nullable private FingerprintManagerCompat fingerprintManager;
	@Nullable private BiometricManager biometricManager;
	private final boolean hasApis;
	private static final String TAG= "FingerprintApiWrapper";

	private static WeakReference<BiometricPrompt.PromptInfo> mPromptInfo;

	FingerprintApiWrapper(@NonNull Context context) {
		// If this is an Application Context, it causes issues when rotating the device while
		// the sensor is active. The 2nd callback will receive the cancellation error of the first
		// authentication action which will immediately onError and unsubscribe the 2nd
		// authentication action.
		if (context instanceof Application) {
			Logger.warn("Passing an Application Context to RxFingerprint might cause issues when the authentication is active and the application changes orientation. Consider passing an Activity Context.");
		}

		this.context = context;

		hasApis = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
		if (hasApis) {
			if(isAbove28()){
				this.biometricManager = getBiometricManager();
				return;
			}
			this.fingerprintManager = getSystemFingerprintManager();
		} else {
			this.fingerprintManager = null;
		}
	}

	boolean isAvailable() {
		return hasApis && isHardwareDetected() && hasEnrolledFingerprints();
	}

	boolean isUnavailable() {
		return !isAvailable();
	}

	boolean isHardwareDetected() {
		if (!hasApis || !fingerprintPermissionGranted()) {
			return false;
		}

		//Do check using biometricManager if the device is greater than or equals API 28
		if(isAbove28()){
			return biometricManager != null && biometricManager.canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS;
		}
		return fingerprintManager != null && fingerprintManager.isHardwareDetected();
	}

	boolean hasEnrolledFingerprints() {
		if (!hasApis || !fingerprintPermissionGranted()) {
			return false;
		}
		if(isAbove28()){
			return biometricManager != null && biometricManager.canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS;
		}
		return fingerprintManager != null && fingerprintManager.hasEnrolledFingerprints();
	}

	FingerprintManagerCompat getFingerprintManager() {
		if (!isAvailable()) {
			throw new IllegalStateException("Device does not support or use Fingerprint APIs. Call isAvailable() before getting FingerprintManager.");
		}
		return fingerprintManager;
	}

	CancellationSignal createCancellationSignal() {
		return new CancellationSignal();
	}

	private boolean fingerprintPermissionGranted() {
		if(isAbove28()){
			return context.checkSelfPermission(USE_BIOMETRIC) == PackageManager.PERMISSION_GRANTED;
		}
		return context.checkSelfPermission(USE_FINGERPRINT) == PackageManager.PERMISSION_GRANTED;
	}

	//This is an ugly code. Please improve it if you can
	public static void setPromptInfo(BiometricPrompt.PromptInfo promptInfo){
		mPromptInfo = new WeakReference<>(promptInfo);
	}

	@Nullable
	private FingerprintManagerCompat getSystemFingerprintManager() {
		try {
			
			return FingerprintManagerCompat.from(context);
		} catch (Exception | NoClassDefFoundError e) {
			Logger.error("Device with SDK >=23 doesn't provide Fingerprint APIs", e);
		}
		return null;
	}


	public void authenticateForBiometric(BiometricHelper cryptoObject,
										 AuthenticationCallbackHelper authenticationCallbackHelper){

		if(context instanceof FragmentActivity){
			authenticate(context,
					((BiometricPrompt.CryptoObject)cryptoObject.getCryptoObject()),
					((BiometricPrompt.AuthenticationCallback)authenticationCallbackHelper.getAuthenticationCallback()));
		}else{
			getFingerprintManager().authenticate(((CryptoObject)cryptoObject.getCryptoObject()),
					0, createCancellationSignal(),
					((AuthenticationCallback)authenticationCallbackHelper.getAuthenticationCallback()),
					null);
		}
	}



	protected  void authenticate(Context context,
								 BiometricPrompt.CryptoObject cryptoObject,
									 BiometricPrompt.AuthenticationCallback callback) {

		BiometricPrompt mBiometricPrompt = new BiometricPrompt(((FragmentActivity)context),
				UIThreadExecutor.get(), callback);

		// Show biometric prompt
		if (cryptoObject != null) {
			Log.i(TAG, "Show biometric prompt_"+ mPromptInfo);
			mBiometricPrompt.authenticate(mPromptInfo.get(), cryptoObject);
		}
	}

	@Nullable
	private BiometricManager getBiometricManager(){
		/*final BiometricManager biometricManager= context.getSystemService(BiometricManager.class);
		if(null != biometricManager){

		}*/
		return context.getSystemService(BiometricManager.class);
	}

	public static boolean isAbove28(){
		return Build.VERSION.SDK_INT >= 29;
	}

	public boolean hasActivity(){
		return (context instanceof FragmentActivity);
	}
}
