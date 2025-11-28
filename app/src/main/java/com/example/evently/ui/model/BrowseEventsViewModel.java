package com.example.evently.ui.model;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.Timestamp;

import com.example.evently.data.EventsDB;
import com.example.evently.data.generic.Promise;
import com.example.evently.data.model.Category;
import com.example.evently.data.model.Event;

/**
 * Shared {@link ViewModel} for Entrant browse event lists and filtering stat
 */
public class BrowseEventsViewModel extends ViewModel {
    private final EventsDB eventsDB = new EventsDB();

    private final MutableLiveData<List<Event>> browseEvents = new MutableLiveData<>(List.of());
    private final MutableLiveData<Set<Category>> selectedCategories =
            new MutableLiveData<>(Collections.emptySet());
    private final MutableLiveData<LocalDateTime> afterDateFilter = new MutableLiveData<>(null);
    private final MutableLiveData<LocalDateTime> beforeDateFilter = new MutableLiveData<>(null);
    private final MutableLiveData<LocalTime> afterTimeFilter = new MutableLiveData<>(null);
    private final MutableLiveData<LocalTime> beforeTimeFilter = new MutableLiveData<>(null);

    private final MediatorLiveData<List<Event>> filteredBrowseEvents = new MediatorLiveData<>();

    /**
     * Creates a new {@link BrowseEventsViewModel}.
     */
    public BrowseEventsViewModel() {
        filteredBrowseEvents.addSource(
                browseEvents,
                events -> filteredBrowseEvents.setValue(applyFilters(
                        events,
                        selectedCategories.getValue(),
                        afterDateFilter.getValue(),
                        beforeDateFilter.getValue(),
                        afterTimeFilter.getValue(),
                        beforeTimeFilter.getValue())));
        filteredBrowseEvents.addSource(
                selectedCategories,
                categories -> filteredBrowseEvents.setValue(applyFilters(
                        browseEvents.getValue(),
                        categories,
                        afterDateFilter.getValue(),
                        beforeDateFilter.getValue(),
                        afterTimeFilter.getValue(),
                        beforeTimeFilter.getValue())));
        filteredBrowseEvents.addSource(
                afterDateFilter,
                afterDate -> filteredBrowseEvents.setValue(applyFilters(
                        browseEvents.getValue(),
                        selectedCategories.getValue(),
                        afterDate,
                        beforeDateFilter.getValue(),
                        afterTimeFilter.getValue(),
                        beforeTimeFilter.getValue())));
        filteredBrowseEvents.addSource(
                beforeDateFilter,
                beforeDate -> filteredBrowseEvents.setValue(applyFilters(
                        browseEvents.getValue(),
                        selectedCategories.getValue(),
                        afterDateFilter.getValue(),
                        beforeDate,
                        afterTimeFilter.getValue(),
                        beforeTimeFilter.getValue())));
        filteredBrowseEvents.addSource(
                afterTimeFilter,
                afterTime -> filteredBrowseEvents.setValue(applyFilters(
                        browseEvents.getValue(),
                        selectedCategories.getValue(),
                        afterDateFilter.getValue(),
                        beforeDateFilter.getValue(),
                        afterTime,
                        beforeTimeFilter.getValue())));
        filteredBrowseEvents.addSource(
                beforeTimeFilter,
                beforeTime -> filteredBrowseEvents.setValue(applyFilters(
                        browseEvents.getValue(),
                        selectedCategories.getValue(),
                        afterDateFilter.getValue(),
                        beforeDateFilter.getValue(),
                        afterTimeFilter.getValue(),
                        beforeTime)));
    }

    /**
     * Exposes the browse events list after applying any selected category filters.
     * @return live updates of the filtered browse events.
     */
    public LiveData<List<Event>> getFilteredBrowseEvents() {
        return filteredBrowseEvents;
    }

    /**
     * Exposes the selected category filters.
     * @return live updates of the selected categories.
     */
    public LiveData<Set<Category>> getSelectedCategories() {
        return selectedCategories;
    }

    /**
     * Exposes the selected lower-bound event date/time filter.
     * @return live updates of the selected lower-bound date/time.
     */
    public LiveData<LocalDateTime> getAfterDateFilter() {
        return afterDateFilter;
    }

    /**
     * Exposes the selected upper-bound event date/time filter.
     * @return live updates of the selected upper-bound date/time.
     */
    public LiveData<LocalDateTime> getBeforeDateFilter() {
        return beforeDateFilter;
    }

    /**
     * Exposes the selected lower-bound event time filter.
     * @return live updates of the selected lower-bound time.
     */
    public LiveData<LocalTime> getAfterTimeFilter() {
        return afterTimeFilter;
    }

    /**
     * Exposes the selected upper-bound event time filter.
     * @return live updates of the selected upper-bound time.
     */
    public LiveData<LocalTime> getBeforeTimeFilter() {
        return beforeTimeFilter;
    }

    /**
     * Refreshes upcoming events for the browse list.
     *
     * @return a {@link Promise} that resolves with the latest upcoming events while also
     *         updating the backing browse list LiveData.
     */
    public Promise<List<Event>> refreshBrowseEvents() {
        return eventsDB.fetchEventsByDate(Timestamp.now(), true).thenRun(browseEvents::setValue);
    }

    /**
     * Updates the active filter categories.
     *
     * @param categories selected categories; empty set clears filters.
     */
    public void setSelectedCategories(Set<Category> categories) {
        selectedCategories.setValue(categories);
    }

    /**
     * Updates the active event date/time filters.
     *
     * @param afterDate only show events occurring on or after this date; {@code null} clears the
     *                  lower date bound.
     * @param beforeDate only show events occurring on or before this date; {@code null} clears the
     *                   upper date bound.
     * @param afterTime only show events occurring at or after this time of day; {@code null} clears
     *                  the lower time bound.
     * @param beforeTime only show events occurring at or before this time of day; {@code null}
     *                   clears the upper time bound.
     */
    public void setDateFilters(
            @Nullable LocalDateTime afterDate,
            @Nullable LocalDateTime beforeDate,
            @Nullable LocalTime afterTime,
            @Nullable LocalTime beforeTime) {
        afterDateFilter.setValue(afterDate);
        beforeDateFilter.setValue(beforeDate);
        afterTimeFilter.setValue(afterTime);
        beforeTimeFilter.setValue(beforeTime);
    }

    /**
     * Clears any filter selection and resets all tracked events.
     */
    public void reset() {
        selectedCategories.setValue(Collections.emptySet());
        browseEvents.setValue(List.of());
    }

    /**
     * Applies the active category filters to the provided events list.
     * @param events events source events to filter; {@code null} is treated as an empty list.
     * @param categories categories selected categories; {@code null} or empty set returns the source
     *                   events unchanged.
     * @return filtered events list.
     */
    private List<Event> applyFilters(
            List<Event> events,
            Set<Category> categories,
            LocalDateTime afterDate,
            LocalDateTime beforeDate,
            LocalTime afterTime,
            LocalTime beforeTime) {
        final var safeEvents = events == null ? List.<Event>of() : events;
        final var safeCategories =
                categories == null ? Collections.<Category>emptySet() : categories;

        final LocalDateTime lowerBound = combineLowerBound(afterDate, afterTime);
        final LocalDateTime upperBound = combineUpperBound(beforeDate, beforeTime);

        final var stream = safeEvents.stream().filter(event -> {
            final var eventDateTime = eventDateTime(event);
            return categoryMatches(event, safeCategories)
                    && (lowerBound == null || !eventDateTime.isBefore(lowerBound))
                    && (upperBound == null || !eventDateTime.isAfter(upperBound));
        });

        return stream.collect(Collectors.toList());
    }

    private boolean categoryMatches(Event event, Set<Category> safeCategories) {

        if (safeCategories.isEmpty()) {
            return true;
        }

        final var categorySet = EnumSet.copyOf(safeCategories);
        return categorySet.contains(event.category());
    }

    private LocalDateTime eventDateTime(Event event) {
        final Instant instant = Instant.ofEpochSecond(
                event.eventTime().getSeconds(), event.eventTime().getNanoseconds());
        return instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    private LocalDateTime combineLowerBound(
            @Nullable LocalDateTime date, @Nullable LocalTime time) {
        if (date != null && time != null) {
            return LocalDateTime.of(date.toLocalDate(), time);
        }

        if (date != null) {
            return LocalDateTime.of(date.toLocalDate(), LocalTime.MIN);
        }

        if (time != null) {
            return LocalDate.now().atTime(time);
        }

        return null;
    }

    private LocalDateTime combineUpperBound(
            @Nullable LocalDateTime date, @Nullable LocalTime time) {
        if (date != null && time != null) {
            return LocalDateTime.of(date.toLocalDate(), time);
        }

        if (date != null) {
            return LocalDateTime.of(date.toLocalDate(), LocalTime.MAX);
        }

        if (time != null) {
            return LocalDate.now().atTime(time);
        }

        return null;
    }
}
