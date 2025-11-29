package com.example.evently.data.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import com.google.firebase.firestore.GeoPoint;

/**
 * Lists of entrants associated with an event.
 * @param eventID The ID of the event in question.
 * @param all List of all the entrants enrolled in said event.
 * @param selected List of the entrants selected via lottery to participate in the event.
 * @param accepted List of selected entrants who have accepted the invitation to participate.
 * @param cancelled List of selected entrants who have declined the invitation to participate.
 */
public record EventEntrants(
        UUID eventID,
        List<String> all,
        List<String> selected,
        List<String> accepted,
        List<String> cancelled,
        HashMap<String, GeoPoint> locations) {

    public EventEntrants(UUID eventID) {
        this(
                eventID,
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new HashMap<>());
    }

    /**
     * Converts the event entrants object to a hashMap for storing in the DB.
     * @implNote The eventID is used as the document path and is not added to the hashmap.
     * @return A hashmap with the event entrant contents.
     */
    public HashMap<String, Object> toHashMap() {
        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("enrolledEntrants", this.all);
        hashMap.put("selectedEntrants", this.selected);
        hashMap.put("acceptedEntrants", this.accepted);
        hashMap.put("cancelledEntrants", this.cancelled);
        hashMap.put("entrantLocations", this.locations);

        return hashMap;
    }
}
