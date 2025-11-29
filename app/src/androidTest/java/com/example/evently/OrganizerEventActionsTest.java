package com.example.evently;

import java.time.Duration;
import java.time.Instant;

import com.google.firebase.Timestamp;

import com.example.evently.data.model.Category;
import com.example.evently.data.model.Event;
import com.example.evently.ui.organizer.OrganizerEventActionsFragment;

public class OrganizerEventActionsTest extends EmulatedFragmentTest<OrganizerEventActionsFragment> {
    @Override
    protected int getGraph() {
        return R.navigation.organizer_graph;
    }

    @Override
    protected Class<OrganizerEventActionsFragment> getFragmentClass() {
        return OrganizerEventActionsFragment.class;
    }

    private Event makeEvent(String name, String organizer) {
        Instant now = Instant.now();
        return new Event(
                name,
                "some event " + name,
                Category.SPORTS,
                new Timestamp(now.plus(Duration.ofHours(1))),
                new Timestamp(now.plus(Duration.ofDays(2))),
                organizer,
                20L);
    }
}
