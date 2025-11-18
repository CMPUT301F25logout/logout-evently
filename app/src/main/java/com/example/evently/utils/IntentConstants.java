package com.example.evently.utils;

/**
 * Constants pertaining to keys in intent extras.
 * Particularly useful for coordinating deep link or external intents.
 * These are intents that may be generated externally (e.g notification click) but contain
 * data we need to navigate the app to the right screen.
 */
public final class IntentConstants {
    public static final String NOTIFICATION_INTENT_ID_KEY = "notificationID";
    public static final String QR_EVENT_INTENT_ID_KEY = "eventID";

    private IntentConstants() {}
}
