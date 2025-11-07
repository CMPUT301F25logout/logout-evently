package com.example.evently;

import static com.example.evently.MatcherUtils.assertRecyclerViewItem;
import static com.example.evently.MatcherUtils.p;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ExecutionException;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.Timestamp;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.example.evently.data.EventsDB;
import com.example.evently.data.model.Category;
import com.example.evently.data.model.Event;
import com.example.evently.ui.organizer.OwnEventsFragment;
import com.example.evently.utils.FirebaseAuthUtils;

@RunWith(AndroidJUnit4.class)
public class OwnEventsFragmentTest extends EmulatedFragmentTest<OwnEventsFragment> {

    private final EventsDB eventsDB = new EventsDB();

    @After
    public void tearDown() throws ExecutionException, InterruptedException {
        eventsDB.nuke().await();
    }

    @Override
    protected int getGraph() {
        return R.navigation.organizer_graph;
    }

    @Override
    protected Class<OwnEventsFragment> getFragmentClass() {
        return OwnEventsFragment.class;
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

    @Test
    public void organizerEventListTest() throws Exception {
        final String self = FirebaseAuthUtils.getCurrentEmail();

        // Create Events
        eventsDB.storeEvent(makeEvent("1", self)).await();
        eventsDB.storeEvent(makeEvent("2", self)).await();
        eventsDB.storeEvent(makeEvent("3", self)).await();
        eventsDB.storeEvent(makeEvent("4", self)).await();
        eventsDB.storeEvent(makeEvent("5", self)).await();
        eventsDB.storeEvent(makeEvent("6", self)).await();
        eventsDB.storeEvent(makeEvent("7", self)).await();
        eventsDB.storeEvent(makeEvent("8", self)).await();
        eventsDB.storeEvent(makeEvent("9", self)).await();
        eventsDB.storeEvent(makeEvent("10", self)).await();

        // Force fragment to re-create so initEvents fetches fresh data
        scenario.recreate();

        // Give the UI a moment to render after async callback
        Thread.sleep(2000);

        // Verify using MatcherUtils
        assertRecyclerViewItem(R.id.event_list, p(R.id.content, "1"));
        assertRecyclerViewItem(R.id.event_list, p(R.id.content, "2"));
        assertRecyclerViewItem(R.id.event_list, p(R.id.content, "3"));
        assertRecyclerViewItem(R.id.event_list, p(R.id.content, "4"));
        assertRecyclerViewItem(R.id.event_list, p(R.id.content, "5"));
        assertRecyclerViewItem(R.id.event_list, p(R.id.content, "6"));
        assertRecyclerViewItem(R.id.event_list, p(R.id.content, "7"));
        assertRecyclerViewItem(R.id.event_list, p(R.id.content, "8"));
        assertRecyclerViewItem(R.id.event_list, p(R.id.content, "9"));
        assertRecyclerViewItem(R.id.event_list, p(R.id.content, "10"));
    }
}
