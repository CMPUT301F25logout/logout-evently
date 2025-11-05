package com.example.evently.data.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

import com.google.firebase.Timestamp;

// TODO (chase): Add image once we decide how to store them.
// TODO (chase): Add location once geolocation support is being worked on.

/**
 * Represents a listed event available for entry.
 * @param eventID The ID of the event
 * @param name The name of the event
 * @param description A brief description about the event available for view to entrants.
 * @param selectionTime Time after which lottery selection will be performed on enlisted entrants.
 *                      Once this time has passed, the event will not be available for entry.
 *                      However, re-selections may take place if invited entrants cancel.
 * @param eventTime Time on which the event is set to happen. No re-selections will take place afterwards.
 * @param organizer email for the organizer. This should correspond with the database.
 * @param entrantLimit Optional limit to the total number of entrants that may enlist before selection.
 * @param selectionLimit Event capacity. This is the total number of enlisted entrants that may be selected.
 * @param entrants emails of all entrants to the event.
 * @param cancelledEntrants emails of entrants who declined enrollment or were cancelled.
 * @param selectedEntrants emails of entrants who were selected to enroll.
 * @param enrolledEntrants emails of final set of enrolled entrants
 * @param category The category of the event.
 */
public record Event(
        UUID eventID,
        String name,
        String description,
        Timestamp selectionTime,
        Timestamp eventTime,
        String organizer,
        Optional<Long> entrantLimit,
        long selectionLimit,
        Collection<String> entrants,
        Collection<String> cancelledEntrants,
        Collection<String> selectedEntrants,
        Collection<String> enrolledEntrants) {

    /**
     * Canonical constructor for event
     * @param eventID The ID of the event
     * @param name The name of the event
     * @param description A brief description about the event available for view to entrants.
     * @param selectionTime Time after which lottery selection will be performed on enlisted entrants.
     *                      Once this time has passed, the event will not be available for entry.
     *                      However, re-selections may take place if invited entrants cancel.
     * @param eventTime Time on which the event is set to happen. No re-selections will take place afterwards.
     * @param organizer email for the organizer. This should correspond with the database.
     * @param entrantLimit Optional limit to the total number of entrants that may enlist before selection.
     * @param selectionLimit Event capacity. This is the total number of enlisted entrants that may be selected.
     * @param entrants emails of all entrants to the event.
     * @param cancelledEntrants emails of entrants who declined enrollment or were cancelled.
     * @param selectedEntrants emails of entrants who were selected to enroll.
     * @param enrolledEntrants emails of final set of enrolled entrants
     */
    public Event {
        if (name.isBlank()) {
            throw new IllegalArgumentException("'name' must not be left blank");
        }

        if (description.isBlank()) {
            throw new IllegalArgumentException("'description' must not be left blank");
        }

        if (eventTime.compareTo(selectionTime) < 1) {
            throw new IllegalArgumentException("'eventTime' must not be before 'selectionTime'");
        }

        if (selectionLimit <= 0) {
            throw new IllegalArgumentException("'selectionLimit' must be positive");
        }

        entrantLimit.ifPresent(limit -> {
            if (limit <= 0) {
                throw new IllegalArgumentException("'entrantLimit' must be positive");
            }
            if (limit < selectionLimit) {
                throw new IllegalArgumentException(
                        "'selectionLimit' must not be lower than 'entrantLimit'.");
            }
        });
    }

    /**
     * Constructor for Event
     * @param eventID The ID of the event
     * @param name The name of the event
     * @param description A brief description about the event available for view to entrants.
     * @param selectionTime Time after which lottery selection will be performed on enlisted entrants.
     *                      Once this time has passed, the event will not be available for entry.
     *                      However, re-selections may take place if invited entrants cancel.
     * @param eventTime Time on which the event is set to happen. No re-selections will take place afterwards.
     * @param organizer email for the organizer. This should correspond with the database.
     * @param entrantLimit Optional limit to the total number of entrants that may enlist before selection.
     * @param selectionLimit Event capacity. This is the total number of enlisted entrants that may be selected.
     */
    public Event(
            UUID eventID,
            String name,
            String description,
            Timestamp selectionTime,
            Timestamp eventTime,
            String organizer,
            Optional<Long> entrantLimit,
            long selectionLimit) {
        this(
                eventID,
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
    }

    /**
     * Constructor for Event
     * @param name The name of the event
     * @param description A brief description about the event available for view to entrants.
     * @param selectionTime Time after which lottery selection will be performed on enlisted entrants.
     *                      Once this time has passed, the event will not be available for entry.
     *                      However, re-selections may take place if invited entrants cancel.
     * @param eventTime Time on which the event is set to happen. No re-selections will take place afterwards.
     * @param organizer email for the organizer. This should correspond with the database.
     * @param entrantLimit Optional limit to the total number of entrants that may enlist before selection.
     * @param selectionLimit Event capacity. This is the total number of enlisted entrants that may be selected.
     */
    public Event(
            String name,
            String description,
            Timestamp selectionTime,
            Timestamp eventTime,
            String organizer,
            Optional<Long> entrantLimit,
            long selectionLimit) {
        this(
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
    }

    /**
     * Converts an event to a hashMap for storing in the DB. Since the eventID
     * is the primary key of the event, it is not added to the hashMap
     * @return A hashmap of the event's contents.
     */
    public HashMap<String, Object> toHashMap() {
        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("name", this.name());
        hashMap.put("description", this.description());
        hashMap.put("selectionTime", this.selectionTime());
        hashMap.put("eventTime", this.eventTime());
        hashMap.put("organizer", this.organizer());
        hashMap.put("entrantLimit", this.entrantLimit().orElse(null));
        hashMap.put("selectionLimit", this.selectionLimit());
        hashMap.put("entrants", this.entrants().toString().replaceAll("[\\[\\]]", ""));
        hashMap.put(
                "cancelledEntrants",
                this.cancelledEntrants().toString().replaceAll("[\\[\\]]", ""));
        hashMap.put(
                "selectedEntrants", this.selectedEntrants().toString().replaceAll("[\\[\\]]", ""));
        hashMap.put(
                "enrolledEntrants", this.enrolledEntrants().toString().replaceAll("[\\[\\]]", ""));

        return hashMap;
    }

    /**
     * Compares this event to another object.
     * @param other the reference object with which to compare.
     * @return {@code false} if the internal values or class is not equal, {@code true} otherwise
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || other.getClass() != getClass()) return false;
        Event event = (Event) other;
        if (!this.eventID().equals(event.eventID())) return false;
        if (!this.name().equals(event.name())) return false;
        if (!this.description().equals(event.description())) return false;
        if (!this.selectionTime().equals(event.selectionTime())) return false;
        if (!this.eventTime().equals(event.eventTime())) return false;
        if (!this.organizer().equals(event.organizer())) return false;
        if (!this.entrantLimit().equals(event.entrantLimit())) return false;
        if (this.selectionLimit() != event.selectionLimit()) return false;
        if (!this.entrants().toString().equals(event.entrants().toString())) return false;
        if (!this.cancelledEntrants()
                .toString()
                .equals(event.cancelledEntrants().toString())) return false;
        if (!this.selectedEntrants().toString().equals(event.selectedEntrants().toString()))
            return false;
        return this.enrolledEntrants()
                .toString()
                .equals(event.enrolledEntrants().toString());
    }
    /**
     * Calculate the status of the event at given time.
     * @param now Time to compare to.
     * @return whether the event is closed or open at given time.
     */
    public EventStatus computeStatus(Instant now) {
        if (now.isBefore(Instant.ofEpochSecond(
                this.selectionTime().getSeconds(), this.selectionTime().getNanoseconds()))) {
            return EventStatus.OPEN;
        } else {
            return EventStatus.CLOSED;
        }
    }
}
