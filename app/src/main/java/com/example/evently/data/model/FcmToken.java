package com.example.evently.data.model;

import java.time.Instant;
import java.util.HashMap;

/**
 * Firebase Cloud Messaging token identifying a device (or in our case a user).
 * This is meant to be stored in the database so cloud functions can utilize it to send notifications.
 * @param token The token given to us by {@link com.google.firebase.messaging.FirebaseMessaging}
 * @param creationTime Time at which the token was handed to us.
 * @see com.example.evently.FirebaseNotificationService
 */
public record FcmToken(String token, Instant creationTime) {

    public HashMap<String, Object> toHashMap() {
        final var mp = new HashMap<String, Object>();
        mp.put("token", token);
        mp.put("timestamp", creationTime);
        return mp;
    }
}
