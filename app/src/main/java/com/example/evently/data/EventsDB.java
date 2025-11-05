package com.example.evently.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import com.example.evently.data.model.Category;
import com.example.evently.data.model.Event;

/**
 * The Event database for managing events
 * @author Ronan St. Amand
 */
public class EventsDB {
    private final CollectionReference eventsRef;

    /**
     * Constructor for EventsDB
     */
    public EventsDB() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        eventsRef = db.collection("events");
    }

    /**
     * Gets an event from a DocumentSnapshot if able
     * @param documentSnapshot DocumentSnapshot to retrieve event from
     * @return An optional with the retrieved event if one was retrieved
     * @throws NullPointerException When documentSnapshot has an incorrectly stored event
     */
    private static Optional<Event> getEventFromSnapshot(DocumentSnapshot documentSnapshot)
            throws NullPointerException {

        if (!documentSnapshot.exists()) return Optional.empty();

        Function<String, ArrayList<String>> unpackList =
                field -> Arrays.stream(documentSnapshot.getString(field).split(","))
                        .collect(Collectors.toCollection(ArrayList::new));

        Optional<Long> optionalEntrantLimit =
                Optional.ofNullable(documentSnapshot.getLong("entrantLimit"));

        return Optional.of(new Event(
                UUID.fromString(documentSnapshot.getId()),
                documentSnapshot.getString("name"),
                documentSnapshot.getString("description"),
                Category.valueOf(documentSnapshot.getString("category")),
                documentSnapshot.getTimestamp("selectionTime"),
                documentSnapshot.getTimestamp("eventTime"),
                documentSnapshot.getString("organizer"),
                documentSnapshot.getLong("selectionLimit"),
                optionalEntrantLimit));
    }

    /**
     * Stores an event in the database.
     * @param event event to be stored
     */
    public void storeEvent(Event event) {
        DocumentReference docRef = eventsRef.document(event.eventID().toString());
        docRef.set(event.toHashMap());
    }

    /**
     * Stores an event in the database.
     * @param event event to be stored
     * @param onSuccess A callback for the onSuccessListener
     * @param onException A callback for the onFailureListener
     */
    public void storeEvent(Event event, Consumer<Void> onSuccess, Consumer<Exception> onException) {
        DocumentReference docRef = eventsRef.document(event.eventID().toString());
        docRef.set(event.toHashMap())
                .addOnSuccessListener(onSuccess::accept)
                .addOnFailureListener(onException::accept);
    }

    /**
     * Fetch an event from database by UUID.
     * @param eventID UUID of the event
     * @param onSuccess A callback for the onSuccessListener
     * @param onException A callback for the onFailureListener
     */
    public void fetchEvent(
            UUID eventID, Consumer<Optional<Event>> onSuccess, Consumer<Exception> onException) {
        eventsRef
                .document(eventID.toString())
                .get()
                .addOnSuccessListener(
                        docSnapshot -> onSuccess.accept(getEventFromSnapshot(docSnapshot)))
                .addOnFailureListener(onException::accept);
    }

    /**
     * Fetch events from database by organizer email.
     * @param organizer email of the event's organizer
     * @param onSuccess A callback for the onSuccessListener
     * @param onException A callback for the onFailureListener
     */
    public void fetchEventsByOrganizers(
            String organizer,
            Consumer<Collection<Event>> onSuccess,
            Consumer<Exception> onException) {
        eventsRef
                .whereEqualTo("organizer", organizer)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Event> events = new ArrayList<>();
                    for (DocumentSnapshot docSnapshot : querySnapshot.getDocuments()) {
                        Optional<Event> event = getEventFromSnapshot(docSnapshot);
                        if (event.isEmpty()) continue;
                        events.add(event.get());
                    }

                    onSuccess.accept(events);
                })
                .addOnFailureListener(onException::accept);
    }

    /**
     * Fetch events before or after a given date
     * @param dateConstraint date to constrain events by
     * @param onSuccess A callback for the onSuccessListener
     * @param onException A callback for the onFailureListener
     * @param isStart {@code true} for events after constraint, {@code false} for events before.
     */
    public void fetchEventsByDate(
            Timestamp dateConstraint,
            Consumer<Collection<Event>> onSuccess,
            Consumer<Exception> onException,
            boolean isStart) {
        Query query;
        if (isStart) {
            query = eventsRef.whereGreaterThan("eventTime", dateConstraint);
        } else {
            query = eventsRef.whereLessThan("eventTime", dateConstraint);
        }
        query.get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Event> events = new ArrayList<>();
                    for (DocumentSnapshot docSnapshot : querySnapshot.getDocuments()) {
                        Optional<Event> event = getEventFromSnapshot(docSnapshot);
                        if (event.isEmpty()) continue;
                        events.add(event.get());
                    }

                    onSuccess.accept(events);
                })
                .addOnFailureListener(onException::accept);
    }

    /**
     * Fetch events from database in a date range.
     * @param startTime Date range start
     * @param endTime Date range end
     * @param onSuccess A callback for the onSuccessListener
     * @param onException A callback for the onFailureListener
     */
    public void fetchEventsByDate(
            Timestamp startTime,
            Timestamp endTime,
            Consumer<QuerySnapshot> onSuccess,
            Consumer<Exception> onException) {
        eventsRef
                .whereGreaterThan("eventTime", startTime)
                .whereLessThan("eventTime", endTime)
                .get()
                .addOnSuccessListener(onSuccess::accept)
                .addOnFailureListener(onException::accept);
    }

    /**
     * Fetch events with an account enrolled.
     * @param enrollee email of enrolled account
     * @param onSuccess A callback for the onSuccessListener
     * @param onException A callback for the onFailureListener
     */
    public void fetchEventsByEnrolled(
            String enrollee, Consumer<QuerySnapshot> onSuccess, Consumer<Exception> onException) {
        eventsRef
                .whereArrayContains("enrolledEntrants", enrollee)
                .get()
                .addOnSuccessListener(onSuccess::accept)
                .addOnFailureListener(onException::accept);
    }

    /**
     * Fetch events with one of the accounts enrolled.
     * @param enrollees emails of enrolled accounts
     * @param onSuccess A callback for the onSuccessListener
     * @param onException A callback for the onFailureListener
     */
    public void fetchEventsByEnrolled(
            List<String> enrollees,
            Consumer<QuerySnapshot> onSuccess,
            Consumer<Exception> onException) {
        eventsRef
                .whereArrayContainsAny("enrolledEntrants", enrollees)
                .get()
                .addOnSuccessListener(onSuccess::accept)
                .addOnFailureListener(onException::accept);
    }

    /**
     * Remove given event from DB
     * @param eventID UUID of event
     */
    public void deleteEvent(UUID eventID) {
        eventsRef.document(eventID.toString()).delete();
    }

    /**
     * Remove given event from DB
     * @param eventID UUID of event
     * @param onSuccess A callback for the onSuccessListener
     * @param onException A callback for the onFailureListener
     */
    public void deleteEvent(
            UUID eventID, Consumer<Void> onSuccess, Consumer<Exception> onException) {
        eventsRef
                .document(eventID.toString())
                .delete()
                .addOnSuccessListener(onSuccess::accept)
                .addOnFailureListener(onException::accept);
    }

    /**
     * Remove given event with String id from DB
     * @param eventID String ID of event
     */
    public void deleteEvent(String eventID) {
        eventsRef.document(eventID).delete();
    }
}
