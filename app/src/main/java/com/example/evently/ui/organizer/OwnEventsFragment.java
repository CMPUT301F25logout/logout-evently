package com.example.evently.ui.organizer;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import com.example.evently.data.model.Category;
import com.example.evently.data.model.Event;
import com.example.evently.ui.common.EventsFragment;

public class OwnEventsFragment extends EventsFragment {

    protected void onEventClick(Event event) {
        // TODO (chase): Organizer event click action?
    }

    @Override
    protected void initEvents(Consumer<List<Event>> callback) {
        // TODO (chase): Get list of own events by organizer.
        var placeHolderEvents = new ArrayList<Event>();
        placeHolderEvents.add(new Event(
                "Trail Running",
                "Let's go trail running across the river valley trails!",
                Instant.parse("2025-11-03T11:59:00.00Z"),
                Instant.parse("2025-11-09T09:00:00.00Z"),
                UUID.randomUUID(),
                Optional.empty(),
                42,
                Category.SPORTS));
        callback.accept(placeHolderEvents);
    }
}
