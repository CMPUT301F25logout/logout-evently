package com.example.evently.ui.entrant;

import java.util.List;
import java.util.function.Consumer;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.Timestamp;

import com.example.evently.R;
import com.example.evently.data.EventsDB;
import com.example.evently.data.model.Event;
import com.example.evently.ui.common.EventsFragment;

/**
 * A fragment representing a list of events the Entrant can join
 */
public class BrowseEventsFragment extends EventsFragment {

    /**
     * Handles clicks on an event row in the Browse list.
     * <p>
     * Uses the Navigation Component to navigate to the Event Details screen,
     * passing the clicked event’s ID as a String argument.
     * @param event The structural representation of the Event view that was clicked.
     */
    @Override
    protected void onEventClick(Event event) {
        // TODO (chase): Navigate to the event details fragment and attach the event ID argument!
        var action = BrowseEventsFragmentDirections.actionNavHomeToEventDetails(
                event.eventID().toString());
        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(action);
    }

    /**
     * Provides the layout used by the Browse screen.
     * @return the layout resource id for the Browse UI and list.
     */
    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_event_entrants_list;
    }

    /**
     * Initializes the Browse tab UI after view inflation.
     * Requires the layout to define {@code @id/btnJoined} and {@code @id/btnBrowse}.
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btnJoined = view.findViewById(R.id.btnJoined);
        Button btnBrowse = view.findViewById(R.id.btnBrowse);

        styleSelected(btnJoined, false);
        styleSelected(btnBrowse, true);

        // Navigate to Joined via action id
        btnJoined.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_browse_to_joined));

        // Already on Browse
        btnBrowse.setOnClickListener(v -> {});
    }

    /**
     * Applies selected/unselected styling to a top tab button.
     * @param b the button to style
     * @param selected true to show the “selected” style or false for unselected.
     */
    private void styleSelected(Button b, boolean selected) {
        if (selected) {
            b.setBackgroundResource(R.drawable.bg_tab_selected);
            b.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));
        } else {
            b.setBackgroundResource(R.drawable.bg_tab_unselected);
            b.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black));
        }
    }

    /**
     * Supplies the Browse list with placeholder events
     * @param callback Callback that will be passed the events into.
     */
    @Override
    protected void initEvents(Consumer<List<Event>> callback) {
        new EventsDB()
                .fetchEventsByDate(Timestamp.now(), true)
                .thenRun(callback)
                .catchE(e -> {
                    Log.e("BrowseEvents", e.toString());
                    Toast.makeText(requireContext(), "Something went wrong...", Toast.LENGTH_SHORT)
                            .show();
                });
    }
}
