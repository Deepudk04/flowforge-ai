package com.flowforge.observability;

import java.util.Set;

public final class LogMasker {
    private static final Set<String> SENSITIVE_KEYS = Set.of(
            "authorization",
            "cookie",
            "set-cookie",
            "password",
            "token",
            "apikey",
            "apiKey",
            "secret"
    );

    private LogMasker() {
    }

    public static String mask(String key, String value) {
        if (value == null) {
            return null;
        }
        if (key == null || !SENSITIVE_KEYS.contains(key)) {
            return value;
        }
        return "[REDACTED]";
    }
}