package com.mtramin.rxfingerprint;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;

class FingerprintApiProvider {

	private FingerprintApiProvider() {
		// hide
	}

	@Nullable
	@RequiresApi(Build.VERSION_CODES.M)
	static FingerprintManager getFingerprintManager(Context context) {
		try {
			return (FingerprintManager) context.getSystemService(Context.FINGERPRINT_SERVICE);
		} catch (Exception | NoClassDefFoundError e) {
			Log.e("RxFingerprint", "Device with SDK >=23 doesn't provide Fingerprint APIs", e);
		}
		return null;
	}


	@SuppressLint("NewApi")
	static CancellationSignal createCancellationSignal() {
		return new CancellationSignal();
	}
}
