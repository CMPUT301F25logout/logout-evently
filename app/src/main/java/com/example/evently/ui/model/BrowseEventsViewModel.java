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

/**
 * Shared {@link ViewModel} for Entrant browse event lists and filtering state.
 */
public class BrowseEventsViewModel extends ViewModel {
    private final EventsDB eventsDB = new EventsDB();

    private final MutableLiveData<List<Event>> browseEvents = new MutableLiveData<>(List.of());
    private final MutableLiveData<Set<Category>> selectedCategories =
            new MutableLiveData<>(Collections.emptySet());

    private final MediatorLiveData<List<Event>> filteredBrowseEvents = new MediatorLiveData<>();

    /**
     * Creates a new {@link BrowseEventsViewModel}.
     */
    public BrowseEventsViewModel() {
        filteredBrowseEvents.addSource(
                browseEvents,
                events -> filteredBrowseEvents.setValue(
                        applyFilters(events, selectedCategories.getValue())));
        filteredBrowseEvents.addSource(
                selectedCategories,
                categories -> filteredBrowseEvents.setValue(
                        applyFilters(browseEvents.getValue(), categories)));
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
