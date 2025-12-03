package com.example.evently.ui.model;

import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.function.Function;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.evently.data.model.Category;
import com.example.evently.data.model.EventFilter;

/**
 * A view model to continually update and track a {@link EventFilter }.
 * User's choices on the filters update this model and {@link com.example.evently.ui.common.LiveEventsFragment }
 * uses it to render the associated events.
 */
public final class EventFilterViewModel extends ViewModel {
    private final MutableLiveData<EventFilter> eventFilterLive =
            new MutableLiveData<>(new EventFilter());

    /**
     * @return The event filter live data to observe changes upon.
     */
    public LiveData<EventFilter> getEventFilter() {
        return eventFilterLive;
    }

    /**
     * Set the given filters to the existing filter, overwriting any previous ones.
     * @param categories Additional chosen categories. Events matching any of these categories should be added.
     * @param startTime Optional event start time to filter by.
     * @param endTime Optional event end time to filter by.
     * @implNote It is important to update all components of the filter at once in order to prevent the observer from
     * firing several times from just a "compound" filter. When the observer fires multiple times, there is no guarantee
     * of the order of event fetching. It's possible that a partially updated filter's event fetching overwrites the effect
     * of the full filter.
     */
    public void setFilters(
            Collection<Category> categories,
            Optional<Instant> startTime,
            Optional<Instant> endTime) {
        modifyLive(currentEventFilter -> new EventFilter(
                new HashSet<>(categories),
                startTime.or(currentEventFilter::startTime),
                endTime.or(currentEventFilter::endTime)));
    }

    /**
     * Clear all filters.
     */
    public void clearAll() {
        eventFilterLive.setValue(new EventFilter());
    }

    // Helper to modify the existing event filter.
    private void modifyLive(Function<EventFilter, EventFilter> mapper) {
        var currentEventFilter = eventFilterLive.getValue();
        if (currentEventFilter == null) {
            currentEventFilter = new EventFilter();
        }
        final var newEventFilter = mapper.apply(currentEventFilter);
        eventFilterLive.setValue(newEventFilter);
    }
}
