package com.mtramin.rxfingerprint;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.support.annotation.RequiresApi;

class FingerprintApiProvider {

	private FingerprintApiProvider() {
		// hide
	}

	@RequiresApi(Build.VERSION_CODES.M)
	static FingerprintManager getFingerprintManager(Context context) {
		return (FingerprintManager) context.getSystemService(Context.FINGERPRINT_SERVICE);
	}

	@SuppressLint("NewApi")
	static CancellationSignal createCancellationSignal() {
		return new CancellationSignal();
	}
}
