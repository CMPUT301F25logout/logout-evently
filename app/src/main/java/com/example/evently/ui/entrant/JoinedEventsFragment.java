package com.example.evently.ui.entrant;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.core.content.ContextCompat;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.Timestamp;

import com.example.evently.R;
import com.example.evently.data.model.Category;
import com.example.evently.data.model.Event;
import com.example.evently.ui.common.EventsFragment;

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
    protected void onEventClick(Event event) {}

    /**
     * Supplies the layout used by this fragment.
     *
     * @return the layout resource id for the “Joined” tab UI and list.
     */
    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_event_entrants_list;
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
        var joined = new ArrayList<Event>();
        joined.add(new Event(
                "Community Piano for Beginners",
                "Intro series for absolute beginners.",
                Category.SPORTS,
                new Timestamp(Instant.parse("2025-12-10T23:59:00Z")),
                new Timestamp(Instant.parse("2026-01-15T18:30:00Z")),
                "orgEmail",
                30));

        joined.add(new Event(
                "Canoe Safety Night",
                "Dryland basics & safety briefing.",
                Category.SPORTS,
                new Timestamp(Instant.parse("2025-11-30T23:59:00Z")),
                new Timestamp(Instant.parse("2026-02-05T19:00:00Z")),
                "orgEmail",
                50));

        joined.add(new Event(
                "Yoga Flow Level 1",
                "Gentle strength and stretch.",
                Category.SPORTS,
                new Timestamp(Instant.parse("2025-12-08T23:59:00Z")),
                new Timestamp(Instant.parse("2026-02-20T09:00:00Z")),
                "orgEmail",
                25,
                25L));

        callback.accept(joined);
    }
}
