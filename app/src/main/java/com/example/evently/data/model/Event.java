package com.example.evently.data.model;

import android.accounts.Account;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

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
 * @param organizer Unique ID for the organizer. This should correspond with the database.
 * @param entrantLimit Optional limit to the total number of entrants that may enlist before selection.
 * @param selectionLimit Event capacity. This is the total number of enlisted entrants that may be selected.
 * @param entrants UUIDs of all entrants to the event.
 * @param cancelledEntrants UUIDs of entrants who declined enrollment or were cancelled.
 * @param selectedEntrants UUIDs of entrants who were selected to enroll.
 * @param enrolledEntrants UUIDs of final set of enrolled entrants
 */
public record Event(
        UUID eventID,
        String name,
        String description,
        Date selectionTime,
        Date eventTime,
        UUID organizer,
        Optional<Long> entrantLimit,
        long selectionLimit,
        Collection<UUID> entrants,
        Collection<UUID> cancelledEntrants,
        Collection<UUID> selectedEntrants,
        Collection<UUID> enrolledEntrants) {
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
        hashMap.put("entrants", this.entrants());
        hashMap.put("cancelledEntrants", this.cancelledEntrants());
        hashMap.put("selectedEntrants", this.selectedEntrants());
        hashMap.put("enrolledEntrants", this.enrolledEntrants());

        return hashMap;
    }
}
