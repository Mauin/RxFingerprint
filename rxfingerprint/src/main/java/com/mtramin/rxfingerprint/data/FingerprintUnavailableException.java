package com.mtramin.rxfingerprint.data;

import android.content.Context;

/**
 * Exception thrown when fingerprint operations are invoked even though the current device doesn't
 * support it.
 * <p>
 * This can be the case if:
 * - The device is on SDK <23 (pre-Marshmallow)
 * - The device doesn't have a fingerprint sensor
 * - The user doesn't have any fingerprints set up
 * - The current application doesn't specify the {@link android.permission.USE_FINGERPRINT}
 * permission
 * <p>
 * To avoid receiving this exception after calling RxFingerprint operations prefer calling
 * {@link com.mtramin.rxfingerprint.RxFingerprint#isAvailable(Context)} to verify fingerprint
 * operations are available.
 */
public class FingerprintUnavailableException extends Exception {

	public FingerprintUnavailableException(String s) {
		super(s);
	}
}
