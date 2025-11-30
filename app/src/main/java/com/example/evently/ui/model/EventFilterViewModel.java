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
public class EventFilterViewModel extends ViewModel {
    private final MutableLiveData<EventFilter> eventFilterLive =
            new MutableLiveData<>(new EventFilter());

    /**
     * @return The event filter live data to observe changes upon.
     */
    public LiveData<EventFilter> getEventFilter() {
        return eventFilterLive;
    }

    /**
     * Set the given categories to the existing filter, overwriting any previous ones.
     * @param categories Additional chosen categories. Events matching any of these categories should be added.
     */
    public void setCategories(Collection<Category> categories) {
        modifyLive(currentEventFilter -> new EventFilter(
                new HashSet<>(categories),
                currentEventFilter.startTime(),
                currentEventFilter.endTime()));
    }

    /**
     * Set a start time filter for events.
     * @apiNote If there was another start time set previously, it will be overwritten with this one.
     * @param start Events that start after this moment may be shown.
     */
    public void setStartTime(Instant start) {
        modifyLive(currentEventFilter -> new EventFilter(
                currentEventFilter.categories(), Optional.of(start), currentEventFilter.endTime()));
    }

    /**
     * Clear the currently set start time.
     */
    public void clearStartTime() {
        modifyLive(currentEventFilter -> new EventFilter(
                currentEventFilter.categories(), Optional.empty(), currentEventFilter.endTime()));
    }

    /**
     * Set a end time filter for events.
     * @apiNote If there was another end time set previously, it will be overwritten with this one.
     * @param end Events that end before this moment may be shown.
     */
    public void setEndTime(Instant end) {
        modifyLive(currentEventFilter -> new EventFilter(
                currentEventFilter.categories(), currentEventFilter.startTime(), Optional.of(end)));
    }

    /**
     * Clear the currently set end time.
     */
    public void clearEndTime() {
        modifyLive(currentEventFilter -> new EventFilter(
                currentEventFilter.categories(), currentEventFilter.startTime(), Optional.empty()));
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
