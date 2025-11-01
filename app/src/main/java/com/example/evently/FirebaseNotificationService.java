package com.example.evently;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.evently.ui.auth.AuthActivity;
import com.example.evently.utils.IntentConstants;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import com.example.evently.utils.FirebaseMessagingUtils;

import java.util.UUID;

public class FirebaseNotificationService extends FirebaseMessagingService {

    private static final String TAG = "FirebaseNotificationService";

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        var dataPayload = remoteMessage.getData();
        var notifPayload = remoteMessage.getNotification();
        if (notifPayload != null && !dataPayload.isEmpty()) {
            // The notifications our cloud functions send always contain both.
            String notifTitle = remoteMessage.getNotification().getTitle();
            String notifBody = remoteMessage.getNotification().getBody();
            UUID notificationID = UUID.fromString(dataPayload.get("id"));
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
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private void sendNotification(String messageTitle, String messageBody, UUID notificationID) {
        // Technically, the notification click should send the user to the entrant notifications page.
        // But! We can't let them bypass auth! Remember that this is a background service.
        // Therefore, we send them to Auth and let auth handle the rest (by passing arguments).
        Intent intent = new Intent(this, AuthActivity.class);
        // The goal is to get to AuthActivity and clear any other activities from the stack.
        // Since AuthActivity is also the root activity, the new task flag works well with it.
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        // Pass the notification ID in the intent.
        intent.putExtra(IntentConstants.NOTIFICATION_INTENT_ID_KEY, notificationID);
        // We set the request code as the UUID hash. There is a possibility of hash collision, but for this to happen
        // for two notifications received around the same time window is unlikely.
        int requestID = notificationID.hashCode();
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, requestID, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        String channelId = getString(R.string.default_notif_channel);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(
                        this, channelId)
                .setSmallIcon(R.drawable.ic_notifs)
                .setContentTitle(messageTitle)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationChannel channel = new NotificationChannel(
                channelId, "Evently", NotificationManager.IMPORTANCE_DEFAULT);
        notificationManager.createNotificationChannel(channel);

        // Once again, we use the hashcode of the UUID for the "unique" push notification ID.
        notificationManager.notify(requestID, notificationBuilder.build());
    }
}
