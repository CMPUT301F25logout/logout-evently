package com.example.evently.ui.common;

/**
 * Confirm fragment for updating name.
 */
public class ConfirmFragmentNameInput extends ConfirmFragmentTextInput {
    /**
     * Check name is valid.
     * @param inp Input name
     * @return Whether or not the name is valid
     */
    protected boolean validateInput(String inp) {
        return !inp.isBlank();
    }
}
