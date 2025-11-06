package com.example.evently.data;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import com.example.evently.data.model.Category;
import com.example.evently.data.model.Event;
import com.example.evently.data.model.EventEntrants;

/**
 * The Event database for managing events
 * @author Ronan St. Amand
 */
public class EventsDB {
    private final CollectionReference eventsRef;
    private final CollectionReference eventEntrantsRef;

    /**
     * Constructor for EventsDB
     */
    public EventsDB() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        eventsRef = db.collection("events");
        eventEntrantsRef = db.collection("eventEntrants");
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
     * Gets an event from a DocumentSnapshot if able
     * @param documentSnapshot DocumentSnapshot to retrieve EventEntrants from
     * @return An optional with the retrieved event entrants struct if one was retrieved
     * @throws NullPointerException When documentSnapshot has an incorrectly stored event entrants struct.
     */
    private static Optional<EventEntrants> getEventEntrantsFromSnapshot(
            DocumentSnapshot documentSnapshot) throws NullPointerException {

        if (!documentSnapshot.exists()) return Optional.empty();

        return Optional.of(new EventEntrants(
                UUID.fromString(documentSnapshot.getId()),
                (List<String>) documentSnapshot.get("enrolledEntrants"),
                (List<String>) documentSnapshot.get("selectedEntrants"),
                (List<String>) documentSnapshot.get("acceptedEntrants"),
                (List<String>) documentSnapshot.get("cancelledEntrants")));
    }

    /**
     * Stores an event in the database.
     * @param event event to be stored
     */
    public void storeEvent(Event event) {
        DocumentReference docRef = eventsRef.document(event.eventID().toString());
        docRef.set(event.toHashMap());
        DocumentReference ref = eventEntrantsRef.document(event.eventID().toString());
        ref.set(new EventEntrants(event.eventID()).toHashMap());
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
                .onSuccessTask(v -> {
                    final var eventId = event.eventID();
                    final var ref = eventEntrantsRef.document(eventId.toString());
                    return ref.set(new EventEntrants(eventId).toHashMap());
                })
                .addOnSuccessListener(onSuccess::accept)
                .addOnFailureListener(onException::accept);
    }

    /**
     * Add a user to the enrolled list of an event.
     * @param eventID Target event.
     * @param email Email of the user to enroll.
     */
    public void enroll(UUID eventID, String email) {
        addEntrantToList(eventID, email, "enrolledEntrants");
    }

    /**
     * Add a user to the selected list of an event.
     * @param eventID Target event.
     * @param email Email of the user to enroll.
     */
    public void addSelected(UUID eventID, String email) {
        addEntrantToList(eventID, email, "selectedEntrants");
    }

    /**
     * Add a user to the accepted list of an event.
     * @param eventID Target event.
     * @param email Email of the user to enroll.
     */
    public void addAccepted(UUID eventID, String email) {
        addEntrantToList(eventID, email, "acceptedEntrants");
    }

    /**
     * Add a user to the cancelled list of an event.
     * @param eventID Target event.
     * @param email Email of the user to enroll.
     */
    public void addCancelled(UUID eventID, String email) {
        addEntrantToList(eventID, email, "cancelledEntrants");
    }

    // Helper to add a user to one of the lists.
    private void addEntrantToList(UUID eventID, String email, String field) {
        final var updateMap = new HashMap<String, Object>();
        updateMap.put(field, FieldValue.arrayUnion(email));
        eventEntrantsRef.document(eventID.toString()).update(updateMap);
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
            Consumer<List<Event>> onSuccess,
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
            Consumer<List<Event>> onSuccess,
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
            String enrollee, Consumer<List<Event>> onSuccess, Consumer<Exception> onException) {
        eventEntrantsRef
                .whereArrayContains("enrolledEntrants", enrollee)
                .get()
                .onSuccessTask(x -> {
                    final var eventIds = x.getDocuments().stream().map(DocumentSnapshot::getId);
                    // TODO: Maybe use ::whereIn (but it can only take 30 elements as match input).
                    final var eventGetTasks = eventIds.map(
                                    entrantId -> eventsRef.document(entrantId).get())
                            .collect(Collectors.toList());
                    return Tasks.<DocumentSnapshot>whenAllSuccess(eventGetTasks);
                })
                .addOnSuccessListener(x -> {
                    final var matchingEvents = x.stream()
                            .map(EventsDB::getEventFromSnapshot)
                            .flatMap(Optional::stream)
                            .collect(Collectors.toList());
                    onSuccess.accept(matchingEvents);
                })
                .addOnFailureListener(onException::accept);
    }

    public void fetchEventEntrants(
            List<UUID> eventIds,
            Consumer<List<EventEntrants>> onSuccess,
            Consumer<Exception> onException) {
        final var eventEntrantGetTasks = eventIds.stream()
                .map(eventId -> eventEntrantsRef.document(eventId.toString()).get())
                .collect(Collectors.toList());
        Tasks.<DocumentSnapshot>whenAllSuccess(eventEntrantGetTasks)
                .addOnSuccessListener(x -> {
                    final var matchingEvents = x.stream()
                            .map(EventsDB::getEventEntrantsFromSnapshot)
                            .flatMap(Optional::stream)
                            .collect(Collectors.toList());
                    onSuccess.accept(matchingEvents);
                })
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
