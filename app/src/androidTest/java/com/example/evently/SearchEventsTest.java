package com.example.evently;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static com.example.evently.MatcherUtils.assertRecyclerViewItem;
import static com.example.evently.MatcherUtils.p;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ExecutionException;

import androidx.navigation.NavGraph;
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
                "1", "description", Category.EDUCATIONAL, selectionTime, eventTime, "orgEmail", 50),
        new Event(
                "2",
                "description1",
                Category.EDUCATIONAL,
                new Timestamp(now.plus(Duration.ofMinutes(1))),
                new Timestamp(now.plus(Duration.ofMinutes(11))),
                "orgEmail",
                50),
        new Event(
                "3",
                "description2",
                Category.EDUCATIONAL,
                new Timestamp(now.plus(Duration.ofMinutes(2))),
                new Timestamp(now.plus(Duration.ofMinutes(12))),
                "orgEmail",
                50),
        new Event(
                "4",
                "description3",
                Category.EDUCATIONAL,
                new Timestamp(now.plus(Duration.ofMinutes(3))),
                new Timestamp(now.plus(Duration.ofMinutes(13))),
                "orgEmail",
                50),
        new Event(
                "5",
                "description4",
                Category.EDUCATIONAL,
                new Timestamp(now.plus(Duration.ofMinutes(4))),
                new Timestamp(now.plus(Duration.ofMinutes(14))),
                "orgEmail",
                50),
        new Event(
                "6",
                "description5",
                Category.EDUCATIONAL,
                new Timestamp(now.plus(Duration.ofMinutes(5))),
                new Timestamp(now.plus(Duration.ofMinutes(15))),
                "orgEmail",
                50),
        new Event(
                "7",
                "description6",
                Category.EDUCATIONAL,
                new Timestamp(now.plus(Duration.ofMinutes(6))),
                new Timestamp(now.plus(Duration.ofMinutes(16))),
                "orgEmail",
                50),
        new Event(
                "8",
                "description7",
                Category.EDUCATIONAL,
                new Timestamp(now.plus(Duration.ofMinutes(7))),
                new Timestamp(now.plus(Duration.ofMinutes(17))),
                "orgEmail",
                50),
        new Event(
                "12345678",
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

        String[] searchTerms = new String[] {"1", "2", "3", "4", "5", "6", "7", "8", "0", "123"};

        Event[][] expectedLists = new Event[][] {
            {mockEvents[0], mockEvents[8]}, // "1"
            {mockEvents[1], mockEvents[8]}, // "2"
            {mockEvents[2], mockEvents[8]}, // "3"
            {mockEvents[3], mockEvents[8]}, // "4"
            {mockEvents[4], mockEvents[8]}, // "5"
            {mockEvents[5], mockEvents[8]}, // "6"
            {mockEvents[6], mockEvents[8]}, // "7"
            {mockEvents[8]}, // "8"
            {}, // "0"
            {mockEvents[8]} // "123"
        };

        for (int i = 0; i < searchTerms.length; i++) {

            // Clear SearchView
            onView(withId(R.id.eventSearch)).perform(typeText(""));
            Thread.sleep(150);

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
                assertRecyclerViewItem(R.id.event_list, p(R.id.content, e.name()));
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
