package com.example.evently.ui.entrant;

import java.util.List;
import java.util.function.Consumer;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.evently.R;
import com.example.evently.data.model.Event;
import com.example.evently.ui.common.LiveEventsFragment;
import com.example.evently.ui.model.BrowseEventsViewModel;

/**
 * A fragment representing a list of events the Entrant can join
 */
public class BrowseEventsFragment extends LiveEventsFragment<List<Event>> {
    private BrowseEventsViewModel eventsViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        eventsViewModel = new ViewModelProvider(requireActivity()).get(BrowseEventsViewModel.class);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final var filtersButton = view.findViewById(R.id.btnBrowseFilters);
        if (filtersButton != null) {
            filtersButton.setOnClickListener(v ->
                    NavHostFragment.findNavController(this).navigate(R.id.action_global_filters));
        }
    }
    /**
     * Handles clicks on an event row in the Browse list.
     * <p>
     * Uses the Navigation Component to navigate to the Event Details screen,
     * passing the clicked event’s ID as a String argument.
     * @param event The structural representation of the Event view that was clicked.
     */
    @Override
    protected void onEventClick(Event event) {
        // The action for clicking on the event, pass the event ID to the next event details
        // fragment
        var action = HomeFragmentDirections.actionNavHomeToEventDetails(event.eventID());
        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(action);
    }

    /**
     * Gets the {@link LiveData} of Browse events.
     * @return the {@link LiveData} of Browse events.
     */
    @Override
    protected LiveData<List<Event>> getLiveData() {
        return eventsViewModel.getFilteredBrowseEvents();
    }

    /**
     * Updates the list of events.
     *
     * @param target the list of events
     * @param act the action to perform on the list
     */
    @Override
    protected void updateEventsBy(List<Event> target, Consumer<List<Event>> act) {
        act.accept(target == null ? List.of() : target);
    }

    /**
     * Gets the layout resource ID for the Browse Events fragment.
     *
     * @return the layout resource ID
     */
    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_browse_events;
    }

    /**
     * Refreshes the list of joined events.
     */
    @Override
    protected void requestRefresh() {
        eventsViewModel.refreshBrowseEvents().catchE(e -> Toast.makeText(
                        requireContext(), "Something went wrong...", Toast.LENGTH_SHORT)
                .show());
    }
}
