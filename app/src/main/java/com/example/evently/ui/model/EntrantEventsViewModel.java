package com.example.evently.ui.model;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.Timestamp;

import com.example.evently.data.EventsDB;
import com.example.evently.data.generic.Promise;
import com.example.evently.data.model.Category;
import com.example.evently.data.model.Event;
import com.example.evently.utils.FirebaseAuthUtils;

/**
 * Shared {@link ViewModel} for Entrant event lists and filtering state.
 */
public class EntrantEventsViewModel extends ViewModel {
    private final EventsDB eventsDB = new EventsDB();

    private final MutableLiveData<List<Event>> browseEvents = new MutableLiveData<>(List.of());
    private final MutableLiveData<List<Event>> joinedEvents = new MutableLiveData<>(List.of());
    private final MutableLiveData<Set<Category>> selectedCategories =
            new MutableLiveData<>(Collections.emptySet());

    private final MediatorLiveData<List<Event>> filteredBrowseEvents = new MediatorLiveData<>();
    private final MediatorLiveData<List<Event>> filteredJoinedEvents = new MediatorLiveData<>();

    public EntrantEventsViewModel() {
        filteredBrowseEvents.addSource(
                browseEvents,
                events -> filteredBrowseEvents.setValue(
                        applyFilters(events, selectedCategories.getValue())));
        filteredBrowseEvents.addSource(
                selectedCategories,
                categories -> filteredBrowseEvents.setValue(
                        applyFilters(browseEvents.getValue(), categories)));

        filteredJoinedEvents.addSource(
                joinedEvents,
                events -> filteredJoinedEvents.setValue(
                        applyFilters(events, selectedCategories.getValue())));
        filteredJoinedEvents.addSource(
                selectedCategories,
                categories -> filteredJoinedEvents.setValue(
                        applyFilters(joinedEvents.getValue(), categories)));
    }

    public LiveData<List<Event>> getFilteredBrowseEvents() {
        return filteredBrowseEvents;
    }

    public LiveData<List<Event>> getFilteredJoinedEvents() {
        return filteredJoinedEvents;
    }

    public LiveData<Set<Category>> getSelectedCategories() {
        return selectedCategories;
    }

    /**
     * Refreshes upcoming events for the browse list.
     */
    public Promise<List<Event>> refreshBrowseEvents() {
        return eventsDB.fetchEventsByDate(Timestamp.now(), true).thenRun(browseEvents::setValue);
    }

    /**
     * Refreshes events the entrant has joined.
     */
    public Promise<List<Event>> refreshJoinedEvents() {
        return eventsDB.fetchEventsByEnrolled(FirebaseAuthUtils.getCurrentEmail())
                .thenRun(joinedEvents::setValue);
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
     * Clears any filter selection and resets all tracked events.
     */
    public void reset() {
        selectedCategories.setValue(Collections.emptySet());
        browseEvents.setValue(List.of());
        joinedEvents.setValue(List.of());
    }

    private List<Event> applyFilters(List<Event> events, Set<Category> categories) {
        final var safeEvents = events == null ? List.<Event>of() : events;
        final var safeCategories =
                categories == null ? Collections.<Category>emptySet() : categories;

        if (safeCategories.isEmpty()) {
            return safeEvents;
        }

        final var categorySet = EnumSet.copyOf(safeCategories);
        return safeEvents.stream()
                .filter(event -> categorySet.contains(event.category()))
                .collect(Collectors.toList());
    }
}
