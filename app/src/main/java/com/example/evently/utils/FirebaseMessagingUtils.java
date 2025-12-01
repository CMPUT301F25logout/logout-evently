package com.example.evently.utils;

import java.time.Instant;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

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
        Log.d("StoreToken", userEmail);

        // Update the token if it's different from the existing one.
        db.collection("fcmTokens").document(userEmail).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                var snapshot = task.getResult();
                var existingToken = snapshot.get("token");
                if (existingToken != null && existingToken.equals(token)) {
                    // Same token, no need to update.
                    return;
                }
            }
            db.collection("fcmTokens").document(userEmail).set(fcmToken.toHashMap());
        });
    }
    /**
     * Enable push notifications for the current user by re-enabling FCM auto-init and storing the
     * latest token.
     */
    public static void enableNotifications() {
        FirebaseMessaging.getInstance().setAutoInitEnabled(true);
        FirebaseMessaging.getInstance()
                .getToken()
                .addOnSuccessListener(FirebaseMessagingUtils::storeToken);
    }

    /**
     * Disable push notifications for the current user by deleting the stored token in Firestore and
     * revoking the current FCM token locally.
     */
    public static void disableNotifications() {
        FirebaseMessaging.getInstance().setAutoInitEnabled(false);
        FirebaseMessaging.getInstance().deleteToken();

        final var userEmail = FirebaseAuthUtils.getCurrentEmail();
        if (userEmail != null) {
            FirebaseFirestore.getInstance()
                    .collection("fcmTokens")
                    .document(userEmail)
                    .delete();
        }
    }

    private FirebaseMessagingUtils() {}
}
