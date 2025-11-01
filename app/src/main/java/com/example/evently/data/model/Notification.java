package com.example.evently.data.model;

import java.util.HashSet;
import java.util.UUID;

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
        return new Notification(id, eventId, channel, title, description, seenByCopy);
    }
}
