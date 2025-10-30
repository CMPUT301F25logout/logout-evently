package com.example.evently.ui.entrant;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import com.example.evently.data.model.Event;
import com.example.evently.ui.common.EventsFragment;

public class BrowseEventsFragment extends EventsFragment {

    @Override
    protected void initEvents(Consumer<List<Event>> callback) {
        // TODO: Replace with “all active events” once DB/Firebase is integrated.
        var placeHolderEvents = new ArrayList<Event>();
        placeHolderEvents.add(new Event(
                "Whale Watching",
                "See whales off the coast — binoculars provided.",
                Instant.parse("2025-12-05T23:59:00Z"), // selection date
                Instant.parse("2026-02-14T09:00:00Z"), // event date
                UUID.randomUUID(),
                Optional.empty(), 
                20));

        placeHolderEvents.add(new Event(
                "LAN Gaming",
                "Bring your rig for co-op action.",
                Instant.parse("2025-12-01T23:59:00Z"),
                Instant.parse("2026-03-09T18:00:00Z"),
                UUID.randomUUID(),
                Optional.empty(),
                64));

        placeHolderEvents.add(new Event(
                "Spelling Bee",
                "Community-wide spelling bee — all ages.",
                Instant.parse("2025-11-28T23:59:00Z"),
                Instant.parse("2026-03-01T13:00:00Z"),
                UUID.randomUUID(),
                Optional.empty(),
                40));

        callback.accept(placeHolderEvents);
    }
}
