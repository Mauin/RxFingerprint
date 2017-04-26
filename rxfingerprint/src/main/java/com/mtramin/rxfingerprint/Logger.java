package com.mtramin.rxfingerprint;

class Logger {
	private static RxFingerprintLogger logger;
	private static boolean shouldLog = true;

	private Logger() {
		// hide
	}

	static void warn(String message) {
		if (!shouldLog) {
			return;
		}

		if (logger == null) {
			createDefaultLogger();
		}
		logger.warn(message);
	}

	static void error(String message, Throwable throwable) {
		if (!shouldLog) {
			return;
		}

		if (logger == null) {
			createDefaultLogger();
		}

		logger.error(message, throwable);
	}

	static void disableLogging() {
		shouldLog = false;
	}

	static void setLogger(RxFingerprintLogger logger) {
		Logger.logger = logger;
	}

	private static void createDefaultLogger() {
		logger = new DefaultLogger();
	}
}
