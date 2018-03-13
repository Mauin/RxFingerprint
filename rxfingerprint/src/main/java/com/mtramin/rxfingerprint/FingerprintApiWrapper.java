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
import android.hardware.fingerprint.FingerprintDialog;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import static android.Manifest.permission.USE_FINGERPRINT;

@SuppressLint("NewApi")
@SuppressWarnings("MissingPermission")
class FingerprintApiWrapper {

	@NonNull private final Context context;
	@Nullable private final FingerprintManager fingerprintManager;
	private final boolean hasApis;
	private final RxFingerprintLogger logger;

	FingerprintApiWrapper(@NonNull Context context, RxFingerprintLogger logger) {
		this.logger = logger;
		// If this is an Application Context, it causes issues when rotating the device while
		// the sensor is active. The 2nd callback will receive the cancellation error of the first
		// authentication action which will immediately onError and unsubscribe the 2nd
		// authentication action.
		if (context instanceof Application) {
			this.logger.warn("Passing an Application Context to RxFingerprint might cause issues when the authentication is active and the application changes orientation. Consider passing an Activity Context.");
		}

		this.context = context;

		hasApis = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
		if (hasApis) {
			this.fingerprintManager = getSystemFingerprintManager();
		} else {
			this.fingerprintManager = null;
		}
	}

	FingerprintDialog buildDialog(FingerprintDialog.Builder fingerprintDialogBuilder) {
		return fingerprintDialogBuilder.build(context);
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

		return fingerprintManager != null && fingerprintManager.isHardwareDetected();
	}

	boolean hasEnrolledFingerprints() {
		if (!hasApis || !fingerprintPermissionGranted()) {
			return false;
		}

		return fingerprintManager != null && fingerprintManager.hasEnrolledFingerprints();
	}

	FingerprintManager getFingerprintManager() {
		if (!isAvailable()) {
			throw new IllegalStateException("Device does not support or use Fingerprint APIs. Call isAvailable() before getting FingerprintManager.");
		}
		return fingerprintManager;
	}

	CancellationSignal createCancellationSignal() {
		return new CancellationSignal();
	}

	private boolean fingerprintPermissionGranted() {
		return context.checkSelfPermission(USE_FINGERPRINT) == PackageManager.PERMISSION_GRANTED;
	}

	@Nullable
	private FingerprintManager getSystemFingerprintManager() {
		try {
			return (FingerprintManager) context.getSystemService(Context.FINGERPRINT_SERVICE);
		} catch (Exception | NoClassDefFoundError e) {
			logger.error("Device with SDK >=23 doesn't provide Fingerprint APIs", e);
		}
		return null;
	}

	/**
	 * Checks if the currently running OS is Android P or above
	 * @return {@code true} if the current Android version is at least Android P
	 */
	static boolean isProfiteroleOrAbove() {
		return Build.VERSION.SDK_INT > 27;
	}
}
