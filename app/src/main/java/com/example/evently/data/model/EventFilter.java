package com.example.evently.data.model;

import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * A filter that may be applied to the collection of events.
 * @param categories List of categories that are chosen. Events from any of these categories will be shown.
 * @param startTime If provided, only events that take place after this moment will be shown.
 * @param endTime If provided, only events that take place before this moment will be shown.
 */
public record EventFilter(
        Set<Category> categories, Optional<Instant> startTime, Optional<Instant> endTime) {
    /**
     * An empty event filter.
     */
    public EventFilter() {
        this(new HashSet<>(), Optional.empty(), Optional.empty());
    }

    public EventFilter(Collection<Category> categories) {
        this(new HashSet<>(categories), Optional.empty(), Optional.empty());
    }
}
