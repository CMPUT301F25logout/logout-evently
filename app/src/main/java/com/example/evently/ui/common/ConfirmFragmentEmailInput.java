package com.example.evently.ui.common;

import android.util.Patterns;

/**
 * Confirm fragment for updating email.
 */
public class ConfirmFragmentEmailInput extends ConfirmFragmentTextInput {
    /**
     * Check email is valid.
     * @param inp Input email
     * @return Whether or not the email is valid
     */
    protected boolean validateInput(String inp) {
        return Patterns.EMAIL_ADDRESS.matcher(inp).matches();
    }
}
