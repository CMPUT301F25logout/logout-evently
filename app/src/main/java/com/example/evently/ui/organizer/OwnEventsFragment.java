package com.example.evently.ui.organizer;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.fragment.NavHostFragment;

import com.example.evently.R;
import com.example.evently.data.model.Category;
import com.example.evently.data.model.Event;
import com.example.evently.ui.common.EventsFragment;

public class OwnEventsFragment extends EventsFragment {

    // Local, mutable list backing the adapter
    private final ArrayList<Event> events = new ArrayList<>();
    protected void onEventClick(Event event) {
        // TODO (chase): Organizer event click action?
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_organizer_event_list;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button create = view.findViewById(R.id.btnCreateEvent);
        if (create != null) {
            create.setOnClickListener(
                    v -> NavHostFragment.findNavController(this).navigate(R.id.navigate_create_events));
        }

        // Receive result from CreateEventFragment (no ViewModel)
        var nav = NavHostFragment.findNavController(this);
        nav.getCurrentBackStackEntry()
                .getSavedStateHandle()
                .<Event>getLiveData("new_event")
                .observe(getViewLifecycleOwner(), event -> {
                    if (event != null) {
                        events.add(event);
                        if (adapter != null) {
                            adapter.notifyItemInserted(events.size() - 1);
                        }
                    }
                });
    }

    @Override
    protected void initEvents(Consumer<List<Event>> callback) {
        if (events.isEmpty()) {
            // seed your original sample
            events.add(new Event(
                    "Trail Running",
                    "Let's go trail running across the river valley trails!",
                    Instant.parse("2025-11-03T11:59:00Z"),
                    Instant.parse("2025-11-09T09:00:00Z"),
                    UUID.randomUUID(),
                    Optional.empty(),
                    42,
                    Category.SPORTS));
        }
        callback.accept(events);
    }
}
