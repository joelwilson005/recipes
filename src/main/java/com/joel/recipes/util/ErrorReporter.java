package com.joel.recipes.util;

import io.sentry.Sentry;

public class ErrorReporter {
    public static void reportError(Throwable e) {
        Sentry.captureException(e);
    }
}
