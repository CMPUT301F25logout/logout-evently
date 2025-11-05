package com.example.evently.ui.organizer;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import com.google.firebase.Timestamp;

import com.example.evently.R;
import com.example.evently.data.model.Event;
import com.example.evently.ui.common.EventsFragment;

public class OwnEventsFragment extends EventsFragment {
    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_event_list;
    }

    @Override
    protected void initEvents(Consumer<List<Event>> callback) {
        // TODO (chase): Get list of own events by organizer.
        var placeHolderEvents = new ArrayList<Event>();
        placeHolderEvents.add(new Event(
                "Trail Running",
                "Let's go trail running across the river valley trails!",
                new Timestamp(Instant.parse("2025-11-03T11:59:00.00Z")),
                new Timestamp(Instant.parse("2025-11-09T09:00:00.00Z")),
                "orgEmail",
                Optional.empty(),
                42));
        callback.accept(placeHolderEvents);
    }
}
