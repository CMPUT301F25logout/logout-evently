package com.example.evently.utils;

import java.time.Instant;

import com.google.firebase.firestore.FirebaseFirestore;

import com.example.evently.data.model.FcmToken;

public final class FirebaseMessagingUtils {

    /**
     * Store given FCM token into Firestore.
     * @param token The token handed to us by FirebaseMessaging.
     */
    public static void storeToken(String token) {
        var db = FirebaseFirestore.getInstance();
        var userEmail = FirebaseAuthUtils.getCurrentEmail();
        var fcmToken = new FcmToken(token, Instant.now());

        assert userEmail != null;

        // Update the token if it's different from the existing one.
        db.collection("fcmTokens").document(userEmail).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                var snapshot = task.getResult();
                var existingToken = snapshot.get("token");
                assert existingToken != null;
                if (existingToken.equals(token)) {
                    // Same token, no need to update.
                    return;
                }
            }
            db.collection("fcmTokens").document(userEmail).set(fcmToken.toHashMap());
        });
    }

    private FirebaseMessagingUtils() {}
}
