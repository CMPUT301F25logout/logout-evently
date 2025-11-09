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

import com.example.evently.R;
import com.example.evently.data.EventsDB;
import com.example.evently.data.model.Event;
import com.example.evently.ui.common.EventsFragment;
import com.example.evently.utils.FirebaseAuthUtils;

/**
 * A fragment representing a list of events that an entrant has joined
 *
 */
public class JoinedEventsFragment extends EventsFragment {

    /**
     * Handles clicks on a joined event item.
     *
     * @param event the clicked {@link Event}.
     */
    @Override
    protected void onEventClick(Event event) {
        // The action for clicking on the event, pass the event ID to the next event details
        // fragment
        var action = JoinedEventsFragmentDirections.actionNavJoinedToEventDetails(event.eventID());
        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(action);
    }

    /**
     * Supplies the layout used by this fragment.
     *
     * @return the layout resource id for the “Joined” tab UI and list.
     */
    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_event_list_tabbed;
    }

    /**
     * Connects top tab buttons and navigation after view inflation.
     *
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btnJoined = view.findViewById(R.id.btnJoined);
        Button btnBrowse = view.findViewById(R.id.btnBrowse);

        styleSelected(btnJoined, true);
        styleSelected(btnBrowse, false);

        // Navigate back to Browse via action id
        btnBrowse.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_joined_to_browse));

        // Already on Joined
        btnJoined.setOnClickListener(v -> {});
    }

    /**
     * Applies selected/unselected styling to a tab button.
     *
     * @param b the button to style
     * @param selected true to show the selected style; false for unselected.
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
     * Supplies the “Joined” list with placeholder events.
     *
     * @param callback Callback that will be passed the events into.
     */
    @Override
    protected void initEvents(Consumer<List<Event>> callback) {
        new EventsDB()
                .fetchEventsByEnrolled(FirebaseAuthUtils.getCurrentEmail())
                .thenRun(callback)
                .catchE(e -> {
                    Log.e("JoinedEvents", e.toString());
                    Toast.makeText(requireContext(), "Something went wrong...", Toast.LENGTH_SHORT)
                            .show();
                });
    }
}
