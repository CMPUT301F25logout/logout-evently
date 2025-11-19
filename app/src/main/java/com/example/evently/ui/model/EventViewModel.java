package com.example.evently.ui.model;

import java.util.UUID;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.example.evently.data.EventsDB;
import com.example.evently.data.model.Event;
import com.example.evently.data.model.EventEntrants;

/**
 * ViewModel to keep track of {@link Event } and {@link EventEntrants } during the lifecycle of
 * an {@link com.example.evently.ui.common.EventDetailsFragment }
 */
public class EventViewModel extends ViewModel {
    public final UUID eventID;
    private final EventsDB eventsDB = new EventsDB();

    private MutableLiveData<Event> event = new MutableLiveData<>();
    private MutableLiveData<EventEntrants> eventEntrants = new MutableLiveData<>();

    public EventViewModel(SavedStateHandle savedStateHandle) {
        eventID = savedStateHandle.get("eventID");
        assert eventID != null;

        requestUpdate();
    }

    public LiveData<Event> getEventLive() {
        return event;
    }

    public LiveData<EventEntrants> getEventEntrantsLive() {
        return eventEntrants;
    }

    public void requestUpdate() {
        requestEventUpdate();
        requestEntrantsUpdate();
    }

    public void requestEventUpdate() {
        eventsDB.fetchEvent(eventID).optionally(eventResult -> {
            event.setValue(eventResult);
        });
    }

    public void requestEntrantsUpdate() {
        eventsDB.fetchEventEntrants(eventID).optionally(eventResult -> {
            eventEntrants.setValue(eventResult);
        });
    }
}
