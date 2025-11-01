package com.example.evently.data.model;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

// TODO (chase): Add image once we decide how to store them.
// TODO (chase): Add location once geolocation support is being worked on.

/**
 * Represents a listed event available for entry.
 *
 * @param name The name of the event.
 * @param description A brief description about the event available for view to entrants.
 * @param selectionTime Time after which lottery selection will be performed on enlisted entrants.
 *                      Once this time has passed, the event will not be available for entry.
 *                      However, re-selections may take place if invited entrants cancel.
 * @param eventTime Time on which the event is set to happen. No re-selections will take place afterwards.
 * @param organizer Unique ID for the organizer. This should correspond with the database.
 * @param entrantLimit Optional limit to the total number of entrants that may enlist before selection.
 * @param selectionLimit Event capacity. This is the total number of enlisted entrants that may be selected.
 * @param category The category of the event.
 */
public record Event(
        String name,
        String description,
        Instant selectionTime,
        Instant eventTime,
        UUID organizer,
        Optional<Long> entrantLimit,
        long selectionLimit,
        Category category) {
    /**
     * Calculate the status of the event at given time.
     * @param now Time to compare to.
     * @return whether the event is closed or open at given time.
     */
    public EventStatus computeStatus(Instant now) {
        if (now.isBefore(this.selectionTime)) {
            return EventStatus.OPEN;
        } else {
            return EventStatus.CLOSED;
        }
    }
}
