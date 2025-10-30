package com.example.evently.data.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Immutable value object representing a Notification.
 *
 * Properties:
 * - associated event ID
 * - channel (all, winners, losers, cancelled)
 * - message
 * - seenBy (list of account emails that have already seen this notification)
 */
public final class Notification {
    private final String id;
    private final String eventId;
    private final NotificationChannel channel;
    private final String message;
    private final List<String> seenByEmails; // lowercased, unique, preserved order of first insert

    private Notification(
            String id,
            String eventId,
            NotificationChannel channel,
            String message,
            List<String> seenByEmails
    ) {
        this.id = requireNonEmpty(id, "id");
        this.eventId = requireNonEmpty(eventId, "eventId");
        this.channel = Objects.requireNonNull(channel, "channel");
        this.message = requireNonEmpty(message, "message");

        // Normalize and uniquify emails, keep insertion order
        List<String> normalized = new ArrayList<>();
        if (seenByEmails != null) {
            Set<String> seen = new HashSet<>();
            for (String e : seenByEmails) {
                if (e == null) continue;
                String lower = e.trim().toLowerCase();
                if (lower.isEmpty()) continue;
                if (seen.add(lower)) {
                    normalized.add(lower);
                }
            }
        }
        this.seenByEmails = Collections.unmodifiableList(normalized);
    }

    public static Notification create(
            String eventId,
            NotificationChannel channel,
            String message
    ) {
        return new Notification(UUID.randomUUID().toString(), eventId, channel, message, List.of());
    }

    public static Notification of(
            String id,
            String eventId,
            NotificationChannel channel,
            String message,
            List<String> seenByEmails
    ) {
        return new Notification(id, eventId, channel, message, seenByEmails);
    }

    public String getId() { return id; }
    public String getEventId() { return eventId; }
    public NotificationChannel getChannel() { return channel; }
    public String getMessage() { return message; }
    public List<String> getSeenByEmails() { return seenByEmails; }

    public boolean hasSeen(String email) {
        if (email == null) return false;
        String lower = email.trim().toLowerCase();
        return seenByEmails.contains(lower);
    }

    /**
     * Returns a new Notification with the given email added to seenBy (if not already present).
     */
    public Notification withSeenByAdded(String email) {
        if (email == null || email.trim().isEmpty()) return this;
        String lower = email.trim().toLowerCase();
        if (seenByEmails.contains(lower)) return this;
        List<String> next = new ArrayList<>(seenByEmails);
        next.add(lower);
        return new Notification(id, eventId, channel, message, next);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Notification that)) return false;
        return id.equals(that.id)
                && eventId.equals(that.eventId)
                && channel == that.channel
                && message.equals(that.message)
                && seenByEmails.equals(that.seenByEmails);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, eventId, channel, message, seenByEmails);
    }

    @Override
    public String toString() {
        return "Notification{" +
                "id='" + id + '\'' +
                ", eventId='" + eventId + '\'' +
                ", channel=" + channel +
                ", message='" + message + '\'' +
                ", seenByEmails=" + seenByEmails +
                '}';
    }

    private static String requireNonEmpty(String value, String name) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " must not be null or empty");
        }
        return value;
    }
}


