package com.example.evently.utils;

import java.util.Objects;

import com.google.firebase.auth.FirebaseAuth;

public final class FirebaseAuthUtils {

    /**
     * This function will throw if called before AuthActivity gets through (i.e user is logged in).
     * @return Email of the currently logged in user.
     */
    public static String getCurrentEmail() {
        var auth = FirebaseAuth.getInstance();
        return Objects.requireNonNull(auth.getCurrentUser()).getEmail();
    }

    private FirebaseAuthUtils() {}
}
