package com.example.evently.utils;

import java.util.regex.Pattern;

import android.util.Patterns;

/**
 * Util for string validation without passing functions
 */
public enum TextInputValidator {
    EMAIL,
    NAME,
    PHONE;

    public Pattern toPattern() {
        return switch (this) {
            case EMAIL -> Patterns.EMAIL_ADDRESS;
            case NAME -> Pattern.compile("^.*?\\S+\\s*?$"); // Non blank string.
            case PHONE -> Patterns.PHONE;
        };
    }
}
