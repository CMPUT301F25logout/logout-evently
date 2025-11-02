package com.example.evently.data;

import com.example.evently.data.model.Notification;
import com.example.evently.data.model.Event;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;
import java.util.function.Consumer;

public class NotificationDB {

    private final CollectionReference notificationsRef;

    public NotificationDB(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        notificationsRef = db.collection("notifications");
    }

    /**
     * Stores a provided notification in the database, with the notification id as the primary key.
     * @param notification The notification to be stored.
     */
    public void storeNotification(Notification notification){
        // Gets notification id
        String notification_id = notification.id().toString();
        DocumentReference docRef = notificationsRef.document(notification_id);
        // Stores the notification in the DB.
        docRef.set(notification.toHashMap());
    }

    /**
     * Creates a notification from a QueryDocumentSnapshot
     * @param snapshot The queryDocumentSnapshot for the notification
     * @return A notification from the QueryDocumentSnapshot.
     */
    private Notification notificationFromQuerySnapshot(QueryDocumentSnapshot snapshot) {
        return new Notification(
                UUID.fromString(snapshot.getString(snapshot.getId())),
                UUID.fromString(snapshot.getString("eventId")),
                // Converts the channel back to an ENUM.
                Notification.Channel.valueOf(snapshot.getString("channel")),
                snapshot.getString("title"),
                snapshot.getString("desc"),
                snapshot.getTimestamp("creationTime").toInstant(),
                (HashSet<String>) snapshot.get("seenBy")
        );
    }

    /**
     * Fetches all notifications, ordered by time sent.
     * a user's notifications.
     * @param onSuccess
     * @param onException
     */
    public void fetchAllNotifications(Consumer<ArrayList<Notification>> onSuccess, Consumer<Exception> onException){

        // The following line of code uses the firebase query-data order-limit-data docs:
        // https://firebase.google.com/docs/firestore/query-data/order-limit-data
        notificationsRef.orderBy("creationTime", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(allDocs -> {
                    // Creates an ArrayList of notifications
                    ArrayList<Notification> notifications = new ArrayList<>();
                    // If we have a notification
                    if (!allDocs.isEmpty()) {

                        // Adds each notification to the list of notifiations.
                        for (QueryDocumentSnapshot doc : allDocs){
                            Notification newNotification = notificationFromQuerySnapshot(doc);
                            notifications.add(newNotification);
                        }
                    }
                    onSuccess.accept(notifications);
                });
    }

    // TODO: Complete once EventDB is completed.
    public void fetchUserNotifications(String email, Consumer<ArrayList<Notification>> onSuccess){

        fetchAllNotifications(
                notifications -> {

                    // Gets all events using future eventDB
                    ArrayList<Event> events = new ArrayList<>();


                }, e -> {});
    }

}
