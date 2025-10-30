package com.example.evently.data;

import com.example.evently.data.model.Event;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.core.Query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

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
     * Creates but does not store an event.
     * @param name The name of the event
     * @param description A brief description about the event available for view to entrants.
     * @param selectionTime Time after which lottery selection will be performed on enlisted entrants.
     *                      Once this time has passed, the event will not be available for entry.
     *                      However, re-selections may take place if invited entrants cancel.
     * @param eventTime Time on which the event is set to happen. No re-selections will take place afterwards.
     * @param organizer Unique ID for the organizer. This should correspond with the database.
     * @param entrantLimit Optional limit to the total number of entrants that may enlist before selection.
     * @param selectionLimit Event capacity. This is the total number of enlisted entrants that may be selected.
     * @return the created event
     */
    public Event createEvent(
            String name,
            String description,
            Date selectionTime,
            Date eventTime,
            UUID organizer,
            Optional<Long> entrantLimit,
            long selectionLimit) {
        Event event = new Event(
                UUID.randomUUID(),
                name,
                description,
                selectionTime,
                eventTime,
                organizer,
                entrantLimit,
                selectionLimit,
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>());
        verifyEvent(event);
        return event;
    }

    /**
     * Verifies an event is valid
     * @param event The event to be verified
     * @throws IllegalArgumentException If one or more parameters of the event were invalid.
     */
    private void verifyEvent(Event event) {
        if (event.name().isBlank()) {
            throw new IllegalArgumentException("'name' must not be left blank");
        }

        if (event.description().isBlank()) {
            throw new IllegalArgumentException("'description' must not be left blank");
        }

        if (event.eventTime().before(event.selectionTime())) {
            throw new IllegalArgumentException("'eventTime' must not be before 'selectionTime'");
        }

        if (event.selectionLimit() <= 0) {
            throw new IllegalArgumentException("'selectionLimit' must be positive");
        }

        event.entrantLimit().ifPresent(limit -> {
            if (limit <= 0) {
                throw new IllegalArgumentException("'entrantLimit' must be positive");
            }
            if (limit < event.selectionLimit()) {
                throw new IllegalArgumentException("'selectionLimit' must not be lower than 'entrantLimit'.");
            }
        });
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
     * Fetch an event from database by UUID.
     * @param eventID UUID of the event
     * @param onSuccess Action to be performed on success
     * @param onException Action to be performed on exception
     */
    public void fetchEvent(UUID eventID, Consumer<DocumentSnapshot> onSuccess, Consumer<Exception> onException) {
        eventsRef
                .document(eventID.toString())
                .get()
                .addOnSuccessListener(onSuccess::accept)
                .addOnFailureListener(onException::accept);
    }

    /**
     * Fetch an event from database by {@code String} eventID.
     * @param eventID eventID of the event
     * @param onSuccess Action to be performed on success
     * @param onException Action to be performed on exception
     */
    public void fetchEvent(String eventID, Consumer<DocumentSnapshot> onSuccess, Consumer<Exception> onException) {
        eventsRef
                .document(eventID)
                .get()
                .addOnSuccessListener(onSuccess::accept)
                .addOnFailureListener(onException::accept);
    }

    /**
     * Fetch events from database by organizer UUID.
     * @param organizer UUID of the event's organizer
     * @param onSuccess Action to be performed on success
     * @param onException Action to be performed on exception
     */
    public void fetchEventsByOrganizers(UUID organizer, Consumer<QuerySnapshot> onSuccess, Consumer<Exception> onException) {
        eventsRef
                .whereEqualTo("organizer", organizer)
                .get()
                .addOnSuccessListener(onSuccess::accept)
                .addOnFailureListener(onException::accept);
    }

    /**
     * Fetch events from database with one of the organizer UUIDs.
     * @param organizers UUID of the event organizers
     * @param onSuccess Action to be performed on success
     * @param onException Action to be performed on exception
     */
    public void fetchEventsByOrganizers(List<UUID> organizers, Consumer<QuerySnapshot> onSuccess, Consumer<Exception> onException) {
        eventsRef
                .whereIn("organizer", organizers)
                .get()
                .addOnSuccessListener(onSuccess::accept)
                .addOnFailureListener(onException::accept);
    }

    /**
     * Fetch events before or after a given date
     * @param dateConstraint date to constrain events by
     * @param onSuccess Action to be performed on success
     * @param onException Action to be performed on exception
     * @param isStart {@code true} for events after constraint, {@code false} for events before.
     */
    public void fetchEventsByDate(Date dateConstraint, Consumer<QuerySnapshot> onSuccess, Consumer<Exception> onException, boolean isStart) {
        if (isStart) {
            eventsRef
                    .whereGreaterThan("eventTime", dateConstraint)
                    .get()
                    .addOnSuccessListener(onSuccess::accept)
                    .addOnFailureListener(onException::accept);
        } else {
            eventsRef
                    .whereLessThan("eventTime", dateConstraint)
                    .get()
                    .addOnSuccessListener(onSuccess::accept)
                    .addOnFailureListener(onException::accept);
        }
    }

    /**
     * Fetch events from database in a date range.
     * @param startTime Date range start
     * @param endTime Date range end
     * @param onSuccess Action to be performed on success
     * @param onException Action to be performed on exception
     */
    public void fetchEventsByDate(Date startTime, Date endTime, Consumer<QuerySnapshot> onSuccess, Consumer<Exception> onException) {
        eventsRef
                .whereGreaterThan("eventTime", startTime)
                .whereLessThan("eventTime", endTime)
                .get()
                .addOnSuccessListener(onSuccess::accept)
                .addOnFailureListener(onException::accept);
    }

    /**
     * Fetch events with an account enrolled.
     * @param enrollee UUID of enrolled account
     * @param onSuccess Action to be performed on success
     * @param onException Action to be performed on exception
     */
    public void fetchEventsByEnrolled(UUID enrollee, Consumer<QuerySnapshot> onSuccess, Consumer<Exception> onException) {
        eventsRef
                .whereArrayContains("enrolledEntrants", enrollee)
                .get()
                .addOnSuccessListener(onSuccess::accept)
                .addOnFailureListener(onException::accept);
    }

    /**
     * Fetch events with one of the accounts enrolled.
     * @param enrollees UUIDs of enrolled accounts
     * @param onSuccess Action to be performed on success
     * @param onException Action to be performed on exception
     */
    public void fetchEventsByEnrolled(List<UUID> enrollees, Consumer<QuerySnapshot> onSuccess, Consumer<Exception> onException) {
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
        eventsRef
                .document(eventID.toString())
                .delete();
    }

    /**
     * Remove given event with String id from DB
     * @param eventID String ID of event
     */
    public void deleteEvent(String eventID) {
        eventsRef
                .document(eventID)
                .delete();
    }



}
