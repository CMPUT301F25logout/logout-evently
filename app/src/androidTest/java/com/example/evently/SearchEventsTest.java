package com.example.evently;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.pressKey;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ExecutionException;

import android.view.KeyEvent;
import androidx.navigation.NavGraph;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
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
import com.example.evently.ui.entrant.EntrantSearchEventsFragment;

/**
 * Test viewing the events in Browsing events
 * @author Vinson Lou
 */
@RunWith(AndroidJUnit4.class)
public class SearchEventsTest extends EmulatedFragmentTest<EntrantSearchEventsFragment> {
    private static final EventsDB eventsDB = new EventsDB();

    private static final Instant now = Instant.now();
    // We can use the same times for these tests.
    private static final Timestamp selectionTime = new Timestamp(now.plus(Duration.ofMillis(100)));
    private static final Timestamp eventTime = new Timestamp(now.plus(Duration.ofMinutes(10)));

    // Create a few events.
    private static final Event[] mockEvents = new Event[] {
        new Event(
                "a",
                "description",
                Category.Educational,
                false,
                selectionTime,
                eventTime,
                "orgEmail",
                50),
        new Event(
                "b",
                "description1",
                Category.Educational,
                false,
                new Timestamp(now.plus(Duration.ofMinutes(1))),
                new Timestamp(now.plus(Duration.ofMinutes(11))),
                "orgEmail",
                50),
        new Event(
                "c",
                "description2",
                Category.Educational,
                false,
                new Timestamp(now.plus(Duration.ofMinutes(2))),
                new Timestamp(now.plus(Duration.ofMinutes(12))),
                "orgEmail",
                50),
        new Event(
                "d",
                "description3",
                Category.Educational,
                false,
                new Timestamp(now.plus(Duration.ofMinutes(3))),
                new Timestamp(now.plus(Duration.ofMinutes(13))),
                "orgEmail",
                50),
        new Event(
                "e",
                "description4",
                Category.Educational,
                false,
                new Timestamp(now.plus(Duration.ofMinutes(4))),
                new Timestamp(now.plus(Duration.ofMinutes(14))),
                "orgEmail",
                50),
        new Event(
                "f",
                "description5",
                Category.Educational,
                false,
                new Timestamp(now.plus(Duration.ofMinutes(5))),
                new Timestamp(now.plus(Duration.ofMinutes(15))),
                "orgEmail",
                50),
        new Event(
                "g",
                "description6",
                Category.Educational,
                false,
                new Timestamp(now.plus(Duration.ofMinutes(6))),
                new Timestamp(now.plus(Duration.ofMinutes(16))),
                "orgEmail",
                50),
        new Event(
                "h",
                "description7",
                Category.Educational,
                false,
                new Timestamp(now.plus(Duration.ofMinutes(7))),
                new Timestamp(now.plus(Duration.ofMinutes(17))),
                "orgEmail",
                50),
        new Event(
                "abcdefgh",
                "description8",
                Category.Educational,
                false,
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

    @AfterClass
    public static void deleteEvents() throws ExecutionException, InterruptedException {
        for (final var mockEvent : mockEvents) {
            eventsDB.deleteEvent(mockEvent.eventID()).await();
        }
    }

    @Test
    public void testSearchEvents() throws InterruptedException {

        // Allow initial load
        Thread.sleep(1500);

        String[] searchTerms = new String[] {"a", "b", "c", "d", "e", "f", "g", "h", "Y", "abc"};

        Event[][] expectedLists = new Event[][] {
            {mockEvents[0], mockEvents[8]}, // "a"
            {mockEvents[1]}, // "b"
            {mockEvents[2]}, // "c"
            {mockEvents[3]}, // "d"
            {mockEvents[4]}, // "e"
            {mockEvents[5]}, // "f"
            {mockEvents[6]}, // "g"
            {mockEvents[7]}, // "h"
            {}, // "Y"
            {mockEvents[8]} // "abc"
        };

        for (int i = 0; i < searchTerms.length; i++) {
            // Clear SearchView (Only deletes 1 character because search is by 1 character)
            onView(withId(R.id.eventSearch)).perform(pressKey(KeyEvent.KEYCODE_DEL));

            // Type new search term
            onView(withId(R.id.eventSearch)).perform(typeText(searchTerms[i]));
            Thread.sleep(500); // allow filtering + adapter update

            Event[] expectedForTerm = expectedLists[i];

            // Assert these events appear
            for (Event e : expectedForTerm) {
                // Makes sure that events exist
                onView(withId(R.id.event_list))
                        .perform(RecyclerViewActions.scrollTo(
                                hasDescendant(allOf(withId(R.id.content), withText(e.name())))));

                // Makes sure that events are actually displayed and visible
                onView(allOf(
                                withId(R.id.content),
                                withText(e.name()),
                                isDescendantOfA(withId(R.id.event_list))))
                        .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
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
    protected Class<EntrantSearchEventsFragment> getFragmentClass() {
        return EntrantSearchEventsFragment.class;
    }
}
