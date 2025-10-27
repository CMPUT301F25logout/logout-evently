package com.example.evently.utils;

import com.example.evently.ui.login.AuthActivity;

/**
 * Configuration constants for {@link AuthActivity} logic.
 */
public final class AuthConstants {
    /**
     * Max number of tries before login flow will give up.
     * <p>
     * Login flows may be interrupted by regular events and they should
     * be retried in that scenario. However, we don't want to get the app stuck
     * in a retry loop.
     */
    public static final int MAX_RETRY = 5;

    private AuthConstants() {}
}
