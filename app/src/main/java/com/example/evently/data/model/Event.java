package com.example.evently.data.model;

import java.io.Serializable;
import java.time.Instant;
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
 * @param category The category of the event.
 * @param selectionTime Time after which lottery selection will be performed on enlisted entrants.
 *                      Once this time has passed, the event will not be available for entry.
 *                      However, re-selections may take place if invited entrants cancel.
 * @param eventTime Time on which the event is set to happen. No re-selections will take place afterwards.
 * @param organizer email for the organizer. This should correspond with the database.
 * @param selectionLimit Event capacity. This is the total number of enlisted entrants that may be selected.
 * @param optionalEntrantLimit Optional limit to the total number of entrants that may enlist before selection.
 */
public record Event(
        UUID eventID,
        String name,
        String description,
        Category category,
        Timestamp selectionTime,
        Timestamp eventTime,
        String organizer,
        long selectionLimit,
        Optional<Long> optionalEntrantLimit) implements Serializable {
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

        optionalEntrantLimit.ifPresent(limit -> {
            if (limit <= 0) {
                throw new IllegalArgumentException("'entrantLimit' must be positive");
            }
            if (limit < selectionLimit) {
                throw new IllegalArgumentException(
                        "'selectionLimit' must not be lower than 'entrantLimit'.");
            }
        });
    }

    public Event(
            String name,
            String description,
            Category category,
            Timestamp selectionTime,
            Timestamp eventTime,
            String organizer,
            long selectionLimit,
            long entrantLimit) {
        this(
                UUID.randomUUID(),
                name,
                description,
                category,
                selectionTime,
                eventTime,
                organizer,
                selectionLimit,
                Optional.of(entrantLimit));
    }

    public Event(
            String name,
            String description,
            Category category,
            Timestamp selectionTime,
            Timestamp eventTime,
            String organizer,
            long selectionLimit) {
        this(
                UUID.randomUUID(),
                name,
                description,
                category,
                selectionTime,
                eventTime,
                organizer,
                selectionLimit,
                Optional.empty());
    }

    /**
     * Converts an event to a hashMap for storing in the DB. Since the eventID
     * is the primary key of the event, it is not added to the hashMap
     * @return A hashmap of the event's contents.
     */
    public HashMap<String, Object> toHashMap() {
        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("name", this.name);
        hashMap.put("description", this.description);
        hashMap.put("category", this.category.toString());
        hashMap.put("selectionTime", this.selectionTime);
        hashMap.put("eventTime", this.eventTime);
        hashMap.put("organizer", this.organizer);
        hashMap.put("selectionLimit", this.selectionLimit);
        hashMap.put("entrantLimit", this.optionalEntrantLimit.orElse(null));

        return hashMap;
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
