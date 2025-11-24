package com.example.evently.ui.model;

import java.time.Instant;
import java.time.LocalDate;
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
    private final MutableLiveData<LocalDate> afterDateFilter = new MutableLiveData<>(null);
    private final MutableLiveData<LocalDate> beforeDateFilter = new MutableLiveData<>(null);

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
                        beforeDateFilter.getValue())));
        filteredBrowseEvents.addSource(
                selectedCategories,
                categories -> filteredBrowseEvents.setValue(applyFilters(
                        browseEvents.getValue(),
                        categories,
                        afterDateFilter.getValue(),
                        beforeDateFilter.getValue())));
        filteredBrowseEvents.addSource(
                afterDateFilter,
                afterDate -> filteredBrowseEvents.setValue(applyFilters(
                        browseEvents.getValue(),
                        selectedCategories.getValue(),
                        afterDate,
                        beforeDateFilter.getValue())));
        filteredBrowseEvents.addSource(
                beforeDateFilter,
                beforeDate -> filteredBrowseEvents.setValue(applyFilters(
                        browseEvents.getValue(),
                        selectedCategories.getValue(),
                        afterDateFilter.getValue(),
                        beforeDate)));
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
     * Exposes the selected lower-bound event date filter.
     * @return live updates of the selected lower-bound date.
     */
    public LiveData<LocalDate> getAfterDateFilter() {
        return afterDateFilter;
    }

    /**
     * Exposes the selected upper-bound event date filter.
     * @return live updates of the selected upper-bound date.
     */
    public LiveData<LocalDate> getBeforeDateFilter() {
        return beforeDateFilter;
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
     * Updates the active event date filters.
     *
     * @param afterDate only show events occurring on or after this date; {@code null} clears the
     *                  lower bound.
     * @param beforeDate only show events occurring on or before this date; {@code null} clears the
     *                   upper bound.
     */
    public void setDateFilters(@Nullable LocalDate afterDate, @Nullable LocalDate beforeDate) {
        afterDateFilter.setValue(afterDate);
        beforeDateFilter.setValue(beforeDate);
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
            LocalDate afterDate,
            LocalDate beforeDate) {
        final var safeEvents = events == null ? List.<Event>of() : events;
        final var safeCategories =
                categories == null ? Collections.<Category>emptySet() : categories;

        final var stream = safeEvents.stream()
                .filter(event -> categoryMatches(event, safeCategories))
                .filter(event -> afterDate == null || !eventDate(event).isBefore(afterDate))
                .filter(event -> beforeDate == null || !eventDate(event).isAfter(beforeDate));

        return stream.collect(Collectors.toList());
    }

    private boolean categoryMatches(Event event, Set<Category> safeCategories) {

        if (safeCategories.isEmpty()) {
            return true;
        }

        final var categorySet = EnumSet.copyOf(safeCategories);
        return categorySet.contains(event.category());
    }

    private LocalDate eventDate(Event event) {
        final Instant instant = Instant.ofEpochSecond(
                event.eventTime().getSeconds(), event.eventTime().getNanoseconds());
        return instant.atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
