package com.mtramin.rxfingerprint.utils;

import android.content.Context;

/**
 * Utility methods for {@link Context}s
 */
public class ContextUtils {

    /**
     * Returns the package name of the current application using this library
     *
     * @param context current context
     * @return Package name of the application
     */
    public static String getPackageName(Context context) {
        return context.getPackageName();
    }
}
