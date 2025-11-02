package com.example.evently.data.model;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import com.google.firebase.Timestamp;

/**
 * A memorized record of a notification in the app.
 * <p>
 * Whenever a notification needs to be sent (e.g winners notification), this record should be created in Firestore.
 * As such, it serves as a "log" of notifications that happen within the app.
 * <p>
 * When such a document is created within Firestore, a cloud function should be triggered that uses
 * Firebase Cloud Messaging to send the actual push notifications to the right entrants (based on Channel).
 * <p>
 * The notifications page should essentially load these records from the database and show them based on recency.
 * The unseen messages should be highlighted to respective entrants.
 * @param id A unique ID to identify the notification. This should be passed in the data payload of notification.
 *           The intent can extract this ID and figure out which notification to extra-highlight in the notification screen
 *           upon the push notification is clicked by the user.
 * @param eventId The event this notification is associated with.
 * @param channel The channel (i.e group of entrants) this notification should be sent/visible to.
 *                Use the events collection to figure out which entrants fall under which channel(s).
 * @param title Title of the notification. This goes into the notification payload.
 * @param description Description of the notification. This goes into the notification payload.
 * @param seenBy Tracker of all the intended entrants who have "seen" this notification (clicked on).
 */
public record Notification(
        UUID id,
        UUID eventId,
        Channel channel,
        String title,
        String description,
        Instant creationTime,
        HashSet<String> seenBy) {
    public enum Channel {
        All,
        Winners,
        Losers,
        Cancelled
    }

    /**
     * @param email Email to identify an app entrant.
     * @return Whether or not user with given email has already seen this Notification or not.
     */
    public boolean hasSeen(String email) {
        String lower = email.trim().toLowerCase();
        return seenBy.contains(lower);
    }

    /**
     * Construct a new {@link Notification} with the new email added to seenBy.
     * @param email New email of user to add to seenBy.
     * @return Copy of this with seenBy changed as described above.
     */
    public Notification addSeen(String email) {
        var seenByCopy = new HashSet<>(seenBy);
        seenByCopy.add(email.trim().toLowerCase());
        return new Notification(id, eventId, channel, title, description, creationTime, seenByCopy);
    }

    /**
     * Returns a hashMap of the given notification, excluding the UUID of the notification.
     * The notification UUID is excluded, as it is the primary key for storing the notification
     * in the DB.
     * @return A HashMap of the location
     */
    public HashMap<String, Object> toHashMap() {
        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("eventId", eventId.toString());
        hashMap.put("channel", channel.toString());
        hashMap.put("title", title);
        hashMap.put("description", description);
        hashMap.put("seenBy", seenBy);

        // The following line code is from Anthropic, Claude Sonnet 4.5:
        // Query: How to store a timestamp in Firebase? I currently
        // have an "Instant" object, and want to store it in a firestore.
        Timestamp timestamp = new Timestamp(creationTime);
        hashMap.put("creationTime", timestamp);

        return hashMap;
    }
}
