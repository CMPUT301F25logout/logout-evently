package com.example.evently.ui.common;

import android.util.Patterns;

/**
 * Confirm fragment for updating phone number.
 */
public class ConfirmFragmentPhoneInput extends ConfirmFragmentTextInput {
    /**
     * Check phone number is valid.
     * @param inp Input phone number
     * @return Whether or not the phone number is valid
     */
    protected boolean validateInput(String inp) {
        return Patterns.PHONE.matcher(inp).matches();
    }
}
