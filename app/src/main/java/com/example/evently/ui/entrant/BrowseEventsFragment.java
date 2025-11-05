package com.example.evently.ui.entrant;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.Timestamp;

import com.example.evently.R;
import com.example.evently.data.model.Category;
import com.example.evently.data.model.Event;
import com.example.evently.ui.common.EventsFragment;

public class BrowseEventsFragment extends EventsFragment {

    @Override
    protected void onEventClick(Event event) {
        // TODO (chase): Navigate to the event details fragment and attach the event ID argument!
        var action = BrowseEventsFragmentDirections.actionNavHomeToEventDetails(
                String.valueOf(event.eventID()));
        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(action);
    }

    @Override
    protected int getLayoutRes() {
        // Layout with the two buttons + list
        return R.layout.fragment_event_entrants_list;
    }

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

    private void styleSelected(Button b, boolean selected) {
        if (selected) {
            b.setBackgroundResource(R.drawable.bg_tab_selected);
            b.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));
        } else {
            b.setBackgroundResource(R.drawable.bg_tab_unselected);
            b.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black));
        }
    }

    @Override
    protected void initEvents(Consumer<List<Event>> callback) {
        // TODO: Replace with “all active events” once DB/Firebase is integrated.
        var browseEvents = new ArrayList<Event>();
        browseEvents.add(new Event(
                "Whale Watching",
                "See whales off the coast — binoculars provided.",
                Category.SOCIAL,
                new Timestamp(Instant.parse("2025-12-05T23:59:00Z")),
                new Timestamp(Instant.parse("2026-02-14T09:00:00Z")),
                "orgEmail",
                20));

        browseEvents.add(new Event(
                "LAN Gaming",
                "Bring your rig for co-op action.",
                Category.SOCIAL,
                new Timestamp(Instant.parse("2025-12-01T23:59:00Z")),
                new Timestamp(Instant.parse("2026-03-09T18:00:00Z")),
                "orgEmail",
                64));

        browseEvents.add(new Event(
                "Spelling Bee",
                "Community-wide spelling bee — all ages.",
                Category.SOCIAL,
                new Timestamp(Instant.parse("2025-11-28T23:59:00Z")),
                new Timestamp(Instant.parse("2026-03-01T13:00:00Z")),
                "orgEmail",
                40));

        callback.accept(browseEvents);
    }
}
