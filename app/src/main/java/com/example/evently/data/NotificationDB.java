package com.example.evently.data;

import static com.example.evently.data.generic.Promise.promise;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import org.jetbrains.annotations.TestOnly;

import com.example.evently.data.generic.Promise;
import com.example.evently.data.model.Event;
import com.example.evently.data.model.EventEntrants;
import com.example.evently.data.model.Notification;

public class NotificationDB {
    /**
     * Helper for fetchUserNotifications down below.
     * @param notifications Notifications obtained from DB.
     * @param eventEntrants EventEntrants associated to the events.
     * @param eventMap EventID to Event mapping.
     */
    private record EventNotificationsInfo(
            QuerySnapshot notifications,
            List<EventEntrants> eventEntrants,
            Map<String, Event> eventMap) {}
    ;

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
    public Promise<Void> storeNotification(Notification notification) {
        // Gets notification id
        String notification_id = notification.id().toString();
        DocumentReference docRef = notificationsRef.document(notification_id);
        // Stores the notification in the DB.
        return promise(docRef.set(notification.toHashMap()));
    }

    public Promise<Void> markSeen(UUID notificationID, String email) {
        final var updateMap = new HashMap<String, Object>();
        updateMap.put("seenBy", FieldValue.arrayUnion(email));
        return promise(notificationsRef.document(notificationID.toString()).update(updateMap));
    }

    /**
     * Creates a notification from a QueryDocumentSnapshot
     *
     * @param snapshot The queryDocumentSnapshot for the notification
     * @return A notification from the QueryDocumentSnapshot.
     */
    private static Notification notificationFromQuerySnapshot(QueryDocumentSnapshot snapshot) {
        ArrayList<String> seenByList = (ArrayList<String>) snapshot.get("seenBy");

        return new Notification(
                UUID.fromString(snapshot.getId()),
                UUID.fromString(snapshot.getString("eventId")),
                // Converts the channel back to an ENUM.
                Notification.Channel.valueOf(snapshot.getString("channel")),
                snapshot.getString("title"),
                snapshot.getString("description"),
                snapshot.getTimestamp("creationTime").toInstant(),
                new HashSet<>(seenByList));
    }

    /**
     * Fetches all notifications, ordered by time sent.
     * a user's notifications.
     */
    public Promise<List<Notification>> fetchAllNotifications() {

        // The following line of code is partially from the firebase query-data order-limit-data
        // docs: https://firebase.google.com/docs/firestore/query-data/order-limit-data
        return promise(notificationsRef
                        .orderBy("creationTime", Query.Direction.DESCENDING)
                        .get())
                .map(NotificationDB::parseQuerySnapshot);
    }

    /**
     * Gets the notifications for a specific event
     * @param eventID     The event being searched for notifications.
     */
    public Promise<List<Notification>> fetchEventNotifications(UUID eventID) {

        // Gets the notifications for an event
        return promise(notificationsRef
                        .whereEqualTo("eventId", eventID.toString())
                        .get())
                .map(NotificationDB::parseQuerySnapshot);
    }

    /**
     * Fetches notifications from the DB by an organizer.
     * @param organizer The organizer of the events which have sent notifications
     */
    public Promise<List<Notification>> fetchNotificationsByOrganizer(String organizer) {
        EventsDB eventsDB = new EventsDB();

        // Fetches event by organizers
        return eventsDB.fetchEventsByOrganizers(organizer)
                .then(eventCollection -> {
                    // Adds fetched eventID's to a list.
                    if (eventCollection.isEmpty()) {
                        // TRAP: There is unfortunately no way to return an empty list as query
                        // snapshot....
                        return Promise.of(null);
                    }
                    final var eventIDs = eventCollection.stream()
                            .map(x -> x.eventID().toString())
                            .collect(Collectors.toList());

                    return promise(notificationsRef.whereIn("eventId", eventIDs).get());
                })
                .map(NotificationDB::parseQuerySnapshot);
    }

    /**
     * Fetches user notifications
     * @param email The user's email
     */
    public Promise<List<Notification>> fetchUserNotifications(String email) {
        EventsDB eventsDB = new EventsDB();

        return eventsDB.fetchEventsByEnrolled(email)
                .thenWith(eventList -> eventsDB.fetchEventEntrants(
                        eventList.stream().map(Event::eventID).collect(Collectors.toList())))
                .then(pair -> {
                    final var events = pair.first;
                    final var eventEntrantsList = pair.second;

                    // Construct an eventID to event map for use later.
                    final Map<String, Event> eventMap = events.stream()
                            .collect(Collectors.toMap(x -> x.eventID().toString(), x -> x));

                    if (events.isEmpty()) {
                        // TRAP: There is unfortunately no way to return an empty list as query
                        // snapshot....
                        return Promise.of(
                                new EventNotificationsInfo(null, eventEntrantsList, eventMap));
                    }

                    // Gets the notifications for the events a user has enrolled..
                    // TODO (chase): Fix this whereIn. It only supports max 30 eventIds.
                    return promise(notificationsRef
                                    // Note: Must be a list of strings.
                                    .whereIn("eventId", new ArrayList<>(eventMap.keySet()))
                                    .get())
                            .map(res ->
                                    new EventNotificationsInfo(res, eventEntrantsList, eventMap));
                })
                .map(eventNotificationsInfo -> {
                    final var allDocsSnapshot = eventNotificationsInfo.notifications;
                    final var eventEntrantsList = eventNotificationsInfo.eventEntrants;
                    final var eventMap = eventNotificationsInfo.eventMap;

                    final var notifications = new ArrayList<Notification>();
                    // This check is needed due to the scenario above.
                    if (allDocsSnapshot == null) {
                        return notifications;
                    }

                    // Tracking which channel "state" an entrant satisfied for an event.
                    // Note: All entrants satisfy the "all" channel state. This is not
                    // tracked separately.
                    final var now = Instant.now();
                    HashMap<UUID, Notification.Channel> entrantChannelInEvent = new HashMap<>();
                    for (final var eventEntrants : eventEntrantsList) {
                        final var event = eventMap.get(eventEntrants.eventID().toString());
                        assert event != null;
                        final var selectionTime = event.selectionTime().toInstant();

                        if (eventEntrants.selected().contains(email)) {
                            entrantChannelInEvent.put(
                                    eventEntrants.eventID(), Notification.Channel.Winners);
                        } else if (selectionTime.isBefore(now)) {
                            entrantChannelInEvent.put(
                                    eventEntrants.eventID(), Notification.Channel.Losers);
                        }
                        if (eventEntrants.cancelled().contains(email)) {
                            entrantChannelInEvent.put(
                                    eventEntrants.eventID(), Notification.Channel.Cancelled);
                        }
                    }

                    // Adds each notification to the notifications list if
                    // it is the
                    // correct channel
                    for (QueryDocumentSnapshot documentSnapshot : allDocsSnapshot) {
                        if (documentSnapshot.exists()) {

                            // Adds the notification list if the message is
                            // to all event
                            // participants or if the user is a member of a
                            // certain
                            // Channel.
                            Notification n = notificationFromQuerySnapshot(documentSnapshot);
                            if (n.channel() == Notification.Channel.All
                                    || n.channel() == entrantChannelInEvent.get(n.eventId())) {
                                notifications.add(n);
                            }
                        }
                    }

                    return notifications;
                });
    }

    @TestOnly
    public Promise<Void> nuke() {
        return promise(notificationsRef.get().onSuccessTask(docs -> {
            WriteBatch batch = FirebaseFirestore.getInstance().batch();
            for (var doc : docs) {
                batch.delete(doc.getReference());
            }
            return batch.commit();
        }));
    }

    private static List<Notification> parseQuerySnapshot(QuerySnapshot snapshot) {
        ArrayList<Notification> notifications = new ArrayList<>();
        // This check is needed due to TRAP scenarios (see above).
        if (snapshot == null) {
            return notifications;
        }

        // Adds each notification to the notifications list
        for (QueryDocumentSnapshot documentSnapshot : snapshot) {
            if (documentSnapshot.exists()) {
                notifications.add(notificationFromQuerySnapshot(documentSnapshot));
            }
        }
        return notifications;
    }
}
