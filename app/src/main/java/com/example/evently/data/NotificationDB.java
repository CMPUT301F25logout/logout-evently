package com.example.evently.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;
import org.jetbrains.annotations.TestOnly;

import com.example.evently.data.model.Event;
import com.example.evently.data.model.Notification;

public class NotificationDB {

    private final CollectionReference notificationsRef;

    public NotificationDB() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        notificationsRef = db.collection("notifications");
    }

    /**
     * Stores a provided notification in the database, with the notification id as the primary key.
     *
     * @param notification The notification to be stored.
     */
    public void storeNotification(Notification notification) {
        // Gets notification id
        String notification_id = notification.id().toString();
        DocumentReference docRef = notificationsRef.document(notification_id);
        // Stores the notification in the DB.
        docRef.set(notification.toHashMap());
    }

    /**
     * Stores a provided notification in the database, with the notification id as the primary key.
     *
     * @param notification The notification to be stored.
     */
    public void storeNotification(
            Notification notification, Consumer<Void> onSuccess, Consumer<Exception> onException) {
        // Gets notification id
        String notification_id = notification.id().toString();
        DocumentReference docRef = notificationsRef.document(notification_id);
        // Stores the notification in the DB.
        docRef.set(notification.toHashMap())
                .addOnSuccessListener(onSuccess::accept)
                .addOnFailureListener(onException::accept);
    }

    public void markSeen(UUID notificationID, String email) {
        final var updateMap = new HashMap<String, Object>();
        updateMap.put("seenBy", FieldValue.arrayUnion(email));
        notificationsRef.document(notificationID.toString()).update(updateMap);
    }

    /**
     * Creates a notification from a QueryDocumentSnapshot
     *
     * @param snapshot The queryDocumentSnapshot for the notification
     * @return A notification from the QueryDocumentSnapshot.
     */
    private Notification notificationFromQuerySnapshot(QueryDocumentSnapshot snapshot) {
        ArrayList<String> seenByList = (ArrayList<String>) snapshot.get("seenBy");

        return new Notification(
                UUID.fromString(snapshot.getString(snapshot.getId())),
                UUID.fromString(snapshot.getString("eventId")),
                // Converts the channel back to an ENUM.
                Notification.Channel.valueOf(snapshot.getString("channel")),
                snapshot.getString("title"),
                snapshot.getString("desc"),
                snapshot.getTimestamp("creationTime").toInstant(),
                new HashSet<>(seenByList));
    }

    /**
     * Fetches all notifications, ordered by time sent.
     * a user's notifications.
     *
     * @param onSuccess Callback to perform on successful query.
     * @param onException Callback to hamdle failure.
     */
    public void fetchAllNotifications(
            Consumer<ArrayList<Notification>> onSuccess, Consumer<Exception> onException) {

        // The following line of code is partially from the firebase query-data order-limit-data
        // docs: https://firebase.google.com/docs/firestore/query-data/order-limit-data
        notificationsRef
                .orderBy("creationTime", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(allDocs -> {
                    // Creates an ArrayList of notifications
                    ArrayList<Notification> notifications = new ArrayList<>();
                    // If we have a notification
                    if (!allDocs.isEmpty()) {
                        // Adds each notification to the list of notifiations.
                        for (QueryDocumentSnapshot doc : allDocs) {
                            Notification newNotification = notificationFromQuerySnapshot(doc);
                            notifications.add(newNotification);
                        }
                    }
                    onSuccess.accept(notifications);
                })
                .addOnFailureListener(onException::accept);
    }

    /**
     * Gets the notifications for a specific event
     * @param eventID     The event being searched for notifications.
     * @param onSuccess   Callback for when the notifications are discovered
     * @param onException Callback for exceptions
     */
    public void fetchEventNotifications(
            UUID eventID,
            Consumer<ArrayList<Notification>> onSuccess,
            Consumer<Exception> onException) {

        // Gets the notifications for an event
        notificationsRef
                .whereEqualTo("eventId", eventID.toString())
                .get()
                .addOnSuccessListener(allDocsSnapshot -> {
                    ArrayList<Notification> notifications = new ArrayList<>();

                    // Adds each notification to the notifications list
                    for (QueryDocumentSnapshot documentSnapshot : allDocsSnapshot) {
                        if (documentSnapshot.exists()) {
                            notifications.add(notificationFromQuerySnapshot(documentSnapshot));
                        }
                    }

                    onSuccess.accept(notifications);
                })
                .addOnFailureListener(onException::accept);
    }

    /**
     * Fetches notifications from the DB by an organizer.
     * @param organizer The organizer of the events which have sent notifications
     * @param onSuccess A callback with the notifications the organizer has created.
     * @param onException A callback for exceptions.
     */
    public void fetchNotificationsByOrganizer(
            String organizer,
            Consumer<ArrayList<Notification>> onSuccess,
            Consumer<Exception> onException) {
        EventsDB eventsDB = new EventsDB();

        // Fetches event by organizers
        eventsDB.fetchEventsByOrganizers(
                organizer,
                eventCollection -> {

                    // Adds fetched eventID's to a list.
                    ArrayList<UUID> eventIDs = new ArrayList<>();
                    for (Event e : eventCollection) eventIDs.add(e.eventID());

                    notificationsRef
                            .whereIn("eventId", eventIDs)
                            .get()
                            .addOnSuccessListener(allDocsSnapshot -> {
                                ArrayList<Notification> notifications = new ArrayList<>();

                                // Adds each notification to the notifications list
                                for (QueryDocumentSnapshot documentSnapshot : allDocsSnapshot) {
                                    if (documentSnapshot.exists()) {
                                        notifications.add(
                                                notificationFromQuerySnapshot(documentSnapshot));
                                    }
                                }
                                onSuccess.accept(notifications);
                            });
                },
                onException);
    }

    /**
     * Fetches unseen notifications from the DB by a user.
     * @param email The user with the email.
     * @param onSuccess A callback of the unseen notifications.
     * @param onException A callback for exceptions.
     */
    public void fetchUnseenNotificationsByUser(
            String email, Consumer<List<Notification>> onSuccess, Consumer<Exception> onException) {

        fetchUserNotifications(
                email,
                notifications -> {

                    // Removes notifications if they have been seen.
                    notifications.removeIf(n -> n.hasSeen(email));
                    onSuccess.accept(notifications);
                },
                onException);
    }

    /**
     * Fetches user notifications
     * @param email The user's email
     * @param onSuccess A callback function of the user's notifications
     * @param onException A callback function for exceptions.
     */
    public void fetchUserNotifications(
            String email,
            Consumer<ArrayList<Notification>> onSuccess,
            Consumer<Exception> onException) {

        EventsDB eventsDB = new EventsDB();

        eventsDB.fetchEventsByEnrolled(
                email,
                eventList -> {
                    if (eventList.isEmpty()) {
                        onSuccess.accept(new ArrayList<>());
                        return;
                    }
                    final var eventIds =
                            eventList.stream().map(Event::eventID).collect(Collectors.toList());
                    eventsDB.fetchEventEntrants(
                            eventIds,
                            eventEntrantsList -> {
                                // Tracking which channel "state" an entrant satisfied for an event.
                                // Note: All entrants satisfy the "all" channel state. This is not
                                // tracked separately.
                                HashMap<UUID, Notification.Channel> entrantChannelInEvent =
                                        new HashMap<>();
                                for (final var eventEntrants : eventEntrantsList) {

                                    if (eventEntrants.selected().contains(email)) {
                                        entrantChannelInEvent.put(
                                                eventEntrants.eventID(),
                                                Notification.Channel.Winners);
                                    } else if (!eventEntrants.selected().isEmpty()) {
                                        entrantChannelInEvent.put(
                                                eventEntrants.eventID(),
                                                Notification.Channel.Losers);
                                    }
                                    if (eventEntrants.cancelled().contains(email)) {
                                        entrantChannelInEvent.put(
                                                eventEntrants.eventID(),
                                                Notification.Channel.Cancelled);
                                    }
                                }
                                // Gets the notifications for the events a user has enrolled..
                                // TODO (chase): Fix this whereIn. It only supports max 30 eventIds.
                                notificationsRef
                                        .whereIn("eventId", eventIds)
                                        .get()
                                        .addOnSuccessListener(allDocsSnapshot -> {
                                            ArrayList<Notification> notifications =
                                                    new ArrayList<Notification>();

                                            // Adds each notification to the notifications list if
                                            // it is the
                                            // correct channel
                                            for (QueryDocumentSnapshot documentSnapshot :
                                                    allDocsSnapshot) {
                                                if (documentSnapshot.exists()) {

                                                    // Adds the notification list if the message is
                                                    // to all event
                                                    // participants or if the user is a member of a
                                                    // certain
                                                    // Channel.
                                                    Notification n = notificationFromQuerySnapshot(
                                                            documentSnapshot);
                                                    if (n.channel() == Notification.Channel.All
                                                            || n.channel()
                                                                    == entrantChannelInEvent.get(
                                                                            n.eventId())) {
                                                        notifications.add(n);
                                                    }
                                                }
                                            }

                                            // Runs the provided callback function.
                                            onSuccess.accept(notifications);
                                        })
                                        .addOnFailureListener(onException::accept);
                            },
                            onException);
                },
                onException);
    }

    @TestOnly
    public Task<Void> nuke() {
        return notificationsRef.get().onSuccessTask(docs -> {
            WriteBatch batch = FirebaseFirestore.getInstance().batch();
            for (var doc : docs) {
                batch.delete(doc.getReference());
            }
            return batch.commit();
        });
    }
}
