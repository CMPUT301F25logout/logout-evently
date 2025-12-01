package com.example.evently.ui.entrant;

import java.util.List;
import java.util.function.Consumer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.evently.data.EventsDB;
import com.example.evently.data.model.Event;
import com.example.evently.data.model.EventFilter;
import com.example.evently.ui.common.LiveEventsFragment;
import com.example.evently.ui.model.EventFilterViewModel;

/**
 * This is the "events list" portion of the browse events page. It observes the event filter view model
 * in order to continually update itself.
 * @see com.example.evently.ui.entrant.BrowseEventsFragment
 * @see EventFilterViewModel
 * @see LiveEventsFragment
 */
public class FilteredEventsFragment extends LiveEventsFragment<EventFilter> {

    private EventFilterViewModel eventFilterViewModel;

    @Override
    protected LiveData<EventFilter> getLiveData() {
        return eventFilterViewModel.getEventFilter();
    }

    @Override
    protected void updateEventsBy(EventFilter filter, Consumer<List<Event>> act) {
        new EventsDB().fetchEventByFilters(filter).thenRun(act);
    }

    @Override
    protected void onEventClick(Event event) {
        var action = HomeFragmentDirections.actionNavHomeToEventDetails(event.eventID());
        NavController navController = NavHostFragment.findNavController(requireParentFragment());
        navController.navigate(action);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        eventFilterViewModel =
                new ViewModelProvider(requireParentFragment()).get(EventFilterViewModel.class);
        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
