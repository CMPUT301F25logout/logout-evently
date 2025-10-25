package com.example.evently.utils.validation;

import android.util.Patterns;

/**
 * Static (effectively) class with the sole goal of providing a function for validating emails.
 * <p>
 * With Java 25, this class can entirely be omitted and the validateEmail function may simply
 * exist at the top level.
 * <p>
 * Example usage:
 * {@code EmailValidator.validate(emailInput); }
 */
public final class EmailValidator {
    private EmailValidator() {}

    /**
     * Uses a standard email pattern to validate a given string as email.
     *
     * <p>
     * Email address validation patterns are one of the most widely disagreed upon
     * patterns in the industry.
     * <p>
     * In this case, we merely use the standard library utils provided. It's likely not perfect but
     * it ought to make do in most cases.
     * @param email User input string
     * @return Whether or not the email is valid.
     */
    public static boolean validate(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}
