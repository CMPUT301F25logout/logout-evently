package com.example.evently.utils;

import android.util.Patterns;

/**
 * Util class for string validation without passing functions
 */
public class TextInputValidator {
    public static final String VALIDATE_EMAIL = "email";
    public static final String VALIDATE_PHONE = "phone";
    public static final String VALIDATE_NAME = "name";
    public static final String VALIDATE_STRING = "string";

    /**
     * Validates string based on given type
     * @param input string to validate
     * @param validateType type of string to validate as
     * @return if string is valid
     */
    public static boolean isValid(String input, String validateType) {
        return switch (validateType) {
            case VALIDATE_EMAIL -> isValidEmail(input);
            case VALIDATE_PHONE -> isValidPhone(input);
            case VALIDATE_NAME -> isValidName(input);
            case VALIDATE_STRING -> input != null;
            default -> false;
        };
    }

    /**
     * Validates string is email
     * @param input string to validate
     * @return if string is valid
     */
    private static boolean isValidEmail(String input) {
        return Patterns.EMAIL_ADDRESS.matcher(input).matches();
    }

    /**
     * Validates string is phone
     * @param input string to validate
     * @return if string is valid
     */
    private static boolean isValidPhone(String input) {
        return Patterns.PHONE.matcher(input).matches();
    }

    /**
     * Validates string is name
     * @param input string to validate
     * @return if string is valid
     */
    private static boolean isValidName(String input) {
        return !input.isBlank();
    }
}
