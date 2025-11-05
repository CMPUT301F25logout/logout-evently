package com.example.evently.ui.entrant;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.core.content.ContextCompat;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.Timestamp;

import com.example.evently.R;
import com.example.evently.data.model.Category;
import com.example.evently.data.model.Event;
import com.example.evently.ui.common.EventsFragment;

public class JoinedEventsFragment extends EventsFragment {

    @Override
    protected void onEventClick(Event event) {}

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_event_entrants_list;
    }

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
