package com.mtramin.rxfingerprint;

/**
 * Logger for RxFingerprint to support custom logging behavior
 */
public interface RxFingerprintLogger {

	/**
	 * Log a warning message
	 *
	 * @param message
	 */
	void warn(String message);

	/**
	 * Log an error message
	 *
	 * @param message
	 */
	void error(String message, Throwable throwable);
}
