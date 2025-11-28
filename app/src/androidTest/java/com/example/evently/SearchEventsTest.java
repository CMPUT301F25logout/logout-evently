package com.example.evently;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.pressKey;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ExecutionException;

import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import androidx.navigation.NavGraph;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.Timestamp;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.example.evently.data.EventsDB;
import com.example.evently.data.generic.Promise;
import com.example.evently.data.model.Category;
import com.example.evently.data.model.Event;
import com.example.evently.ui.entrant.SearchEventsFragment;

/**
 * Test viewing the events in Browsing events
 * @author Vinson Lou
 */
@RunWith(AndroidJUnit4.class)
public class SearchEventsTest extends EmulatedFragmentTest<SearchEventsFragment> {
    private static final EventsDB eventsDB = new EventsDB();

    private static final Instant now = Instant.now();
    // We can use the same times for these tests.
    private static final Timestamp selectionTime = new Timestamp(now.plus(Duration.ofMillis(100)));
    private static final Timestamp eventTime = new Timestamp(now.plus(Duration.ofMinutes(10)));

    // Create a few events.
    private static final Event[] mockEvents = new Event[] {
        new Event(
                "a", "description", Category.EDUCATIONAL, selectionTime, eventTime, "orgEmail", 50),
        new Event(
                "b",
                "description1",
                Category.EDUCATIONAL,
                new Timestamp(now.plus(Duration.ofMinutes(1))),
                new Timestamp(now.plus(Duration.ofMinutes(11))),
                "orgEmail",
                50),
        new Event(
                "c",
                "description2",
                Category.EDUCATIONAL,
                new Timestamp(now.plus(Duration.ofMinutes(2))),
                new Timestamp(now.plus(Duration.ofMinutes(12))),
                "orgEmail",
                50),
        new Event(
                "d",
                "description3",
                Category.EDUCATIONAL,
                new Timestamp(now.plus(Duration.ofMinutes(3))),
                new Timestamp(now.plus(Duration.ofMinutes(13))),
                "orgEmail",
                50),
        new Event(
                "e",
                "description4",
                Category.EDUCATIONAL,
                new Timestamp(now.plus(Duration.ofMinutes(4))),
                new Timestamp(now.plus(Duration.ofMinutes(14))),
                "orgEmail",
                50),
        new Event(
                "f",
                "description5",
                Category.EDUCATIONAL,
                new Timestamp(now.plus(Duration.ofMinutes(5))),
                new Timestamp(now.plus(Duration.ofMinutes(15))),
                "orgEmail",
                50),
        new Event(
                "g",
                "description6",
                Category.EDUCATIONAL,
                new Timestamp(now.plus(Duration.ofMinutes(6))),
                new Timestamp(now.plus(Duration.ofMinutes(16))),
                "orgEmail",
                50),
        new Event(
                "h",
                "description7",
                Category.EDUCATIONAL,
                new Timestamp(now.plus(Duration.ofMinutes(7))),
                new Timestamp(now.plus(Duration.ofMinutes(17))),
                "orgEmail",
                50),
        new Event(
                "abcdefgh",
                "description8",
                Category.EDUCATIONAL,
                new Timestamp(now.plus(Duration.ofMinutes(8))),
                new Timestamp(now.plus(Duration.ofMinutes(18))),
                "orgEmail",
                50)
    };

    @BeforeClass
    public static void storeEvents() throws ExecutionException, InterruptedException {
        final var self = FirebaseEmulatorTest.defaultMockAccount.email();

        // Store events into DB
        for (int i = 0; i < mockEvents.length; i++) {
            eventsDB.storeEvent(mockEvents[i]).await();

            if (i % 2 == 0) {
                eventsDB.unsafeEnroll(mockEvents[i].eventID(), self).await();
            }
        }
    }

    @Test
    public void testSearchEvents() throws InterruptedException {

        // Allow initial load
        Thread.sleep(1500);

        String[] searchTerms = new String[] {"a", "b", "c", "d", "e", "f", "g", "h", " ", "abc"};

        Event[][] expectedLists = new Event[][] {
            {mockEvents[0], mockEvents[8]}, // "a"
            {mockEvents[1], mockEvents[8]}, // "b"
            {mockEvents[2], mockEvents[8]}, // "c"
            {mockEvents[3], mockEvents[8]}, // "d"
            {mockEvents[4], mockEvents[8]}, // "e"
            {mockEvents[5], mockEvents[8]}, // "f"
            {mockEvents[6], mockEvents[8]}, // "g"
            {mockEvents[8]}, // "h"
            {}, // " "
            {mockEvents[8]} // "abc"
        };

        for (int i = 0; i < searchTerms.length; i++) {

            // Clear SearchView (Only deletes 1 character because search is by 1 character)
            onView(withId(R.id.eventSearch)).perform(pressKey(KeyEvent.KEYCODE_DEL));

            // Type new search term
            onView(withId(R.id.eventSearch)).perform(typeText(searchTerms[i]));
            Thread.sleep(1000); // allow filtering + adapter update

            Event[] expectedForTerm = expectedLists[i];

            if (expectedForTerm.length == 0) {
                // Nothing should be visible
                onView(withId(R.id.event_list)).check(matches(isDisplayed()));
                continue;
            }

            // Assert these events appear
            for (Event e : expectedForTerm) {
                onView(withId(R.id.event_list)).check((view, noViewFoundException) -> {
                    RecyclerView recyclerView = (RecyclerView) view;

                    for (int j = 0; j < recyclerView.getChildCount(); j++) {
                        View child = recyclerView.getChildAt(j); // each visible row
                        TextView textView =
                                child.findViewById(R.id.content); // your TextView inside row
                        if (textView != null) {
                            String eventName = textView.getText().toString();
                            System.out.println("Visible event: " + eventName);
                        }
                    }
                });
            }
        }
    }

    @AfterClass
    public static void tearDownEvents() throws ExecutionException, InterruptedException {
        Promise.all(eventsDB.nuke()).await();
    }

    @Override
    protected int getGraph() {
        return R.navigation.entrant_graph;
    }

    @Override
    protected int getSelfDestination(NavGraph graph) {
        return R.id.nav_home;
    }

    @Override
    protected Class<SearchEventsFragment> getFragmentClass() {
        return SearchEventsFragment.class;
    }
}
