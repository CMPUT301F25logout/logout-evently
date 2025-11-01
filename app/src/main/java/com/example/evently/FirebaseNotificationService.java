package com.example.evently;

import java.util.UUID;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import com.example.evently.ui.auth.AuthActivity;
import com.example.evently.utils.FirebaseMessagingUtils;
import com.example.evently.utils.IntentConstants;

/**
 * Background service that facilitates the receiving of push notifications via firebase cloud messaging.
 * <p>
 * The goal of the overridden code in this service is twofold.
 * <p>
 * First: to persist any new messaging tokens into our database so our cloud functions
 * can send push notifications to the proper devices.
 * <p>
 * Second: to receive any messages while on foreground and turn them into push notifications with
 * the right click action. i.e clicking on them will bring the user to the notifications page.
 */
public class FirebaseNotificationService extends FirebaseMessagingService {

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        final var dataPayload = remoteMessage.getData();
        final var notifPayload = remoteMessage.getNotification();
        if (notifPayload != null && !dataPayload.isEmpty()) {
            // The notifications our cloud functions send always contain both.
            final var notifTitle = remoteMessage.getNotification().getTitle();
            final var notifBody = remoteMessage.getNotification().getBody();
            final var notificationID = UUID.fromString(dataPayload.get("id"));
            if (notifTitle != null && notifBody != null && notificationID != null) {
                sendNotification(notifTitle, notifBody, notificationID);
            }
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        // Store the token into firestore.
        FirebaseMessagingUtils.storeToken(token);
    }

    /**
     * Create and send a push notification with the an associated click action that brings the user
     * to the identified notification in the entrant notifications page.
     * @param messageTitle The title of the notification. Should be the same as what was received by FCM.
     * @param messageBody The body of the notification. Should be the same as what was received by FCM.
     * @param notificationID The ID of the notification in our database.
     *                       This should have been received in the data payload of the associated FCM.
     */
    private void sendNotification(String messageTitle, String messageBody, UUID notificationID) {
        // Technically, the notification click should send the user to the entrant notifications
        // page.
        // But! We can't let them bypass auth! Remember that this is a background service.
        // Therefore, we send them to Auth and let auth handle the rest (by passing arguments).
        final var intent = new Intent(this, AuthActivity.class);
        // The goal is to get to AuthActivity and clear any other activities from the stack.
        // Since AuthActivity is also the root activity, the new task flag works well with it.
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        // Pass the notification ID in the intent.
        intent.putExtra(IntentConstants.NOTIFICATION_INTENT_ID_KEY, notificationID);
        // We set the request code as the UUID hash. There is a possibility of hash collision, but
        // for this to happen
        // for two notifications received around the same time window is unlikely.
        final var requestID = notificationID.hashCode();
        final var pendingIntent = PendingIntent.getActivity(
                this,
                requestID,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        final var channelId = getString(R.string.default_notif_channel);
        final var defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        final var notificationBuilder = new NotificationCompat.Builder(
                        this, channelId)
                .setSmallIcon(R.drawable.ic_notifs)
                .setContentTitle(messageTitle)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        final var notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        final var channel = new NotificationChannel(
                channelId, "Evently", NotificationManager.IMPORTANCE_DEFAULT);
        notificationManager.createNotificationChannel(channel);

        // Once again, we use the hashcode of the UUID for the "unique" push notification ID.
        notificationManager.notify(requestID, notificationBuilder.build());
    }
}
