package com.example.evently;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static com.example.evently.MatcherUtils.assertRecyclerViewItem;
import static com.example.evently.MatcherUtils.p;

import java.text.MessageFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutionException;

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
import com.example.evently.ui.entrant.BrowseEventsFragment;

/**
 * Test viewing the events in Browsing events
 * @author Vinson Lou
 */
@RunWith(AndroidJUnit4.class)
public class BrowseEventsTest extends EmulatedFragmentTest<BrowseEventsFragment> {
    private static final EventsDB eventsDB = new EventsDB();

    private static final Instant now = Instant.now();
    // We can use the same times for these tests.\

    private static final DateTimeFormatter SELECTION_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault());
    private static final DateTimeFormatter EVENT_DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());
    private static final Timestamp selectionTime = new Timestamp(now.plus(Duration.ofMillis(100)));
    private static final Timestamp eventTime = new Timestamp(now.plus(Duration.ofMinutes(10)));

    // Create a few events.
    private static final Event[] mockEvents = new Event[] {
        new Event(
                "name",
                "description",
                Category.EDUCATIONAL,
                selectionTime,
                eventTime,
                "orgEmail",
                50),
        new Event(
                "name1",
                "description1",
                Category.EDUCATIONAL,
                new Timestamp(now.plus(Duration.ofMinutes(1))),
                new Timestamp(now.plus(Duration.ofMinutes(11))),
                "orgEmail",
                50),
        new Event(
                "name2",
                "description2",
                Category.EDUCATIONAL,
                new Timestamp(now.plus(Duration.ofMinutes(2))),
                new Timestamp(now.plus(Duration.ofMinutes(12))),
                "orgEmail",
                50),
        new Event(
                "name3",
                "description3",
                Category.EDUCATIONAL,
                new Timestamp(now.plus(Duration.ofMinutes(3))),
                new Timestamp(now.plus(Duration.ofMinutes(13))),
                "orgEmail",
                50),
        new Event(
                "name4",
                "description4",
                Category.EDUCATIONAL,
                new Timestamp(now.plus(Duration.ofMinutes(4))),
                new Timestamp(now.plus(Duration.ofMinutes(14))),
                "orgEmail",
                50),
        new Event(
                "name5",
                "description5",
                Category.EDUCATIONAL,
                new Timestamp(now.plus(Duration.ofMinutes(5))),
                new Timestamp(now.plus(Duration.ofMinutes(15))),
                "orgEmail",
                50),
        new Event(
                "name6",
                "description6",
                Category.EDUCATIONAL,
                new Timestamp(now.plus(Duration.ofMinutes(6))),
                new Timestamp(now.plus(Duration.ofMinutes(16))),
                "orgEmail",
                50),
        new Event(
                "name7",
                "description7",
                Category.EDUCATIONAL,
                new Timestamp(now.plus(Duration.ofMinutes(7))),
                new Timestamp(now.plus(Duration.ofMinutes(17))),
                "orgEmail",
                50),
        new Event(
                "name8",
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
    public void testViewingEvents() throws InterruptedException {
        Thread.sleep(2000);

        // Test if each event added shows up on the recyclerview, one of the events is closed
        for (final var expectedEvent : mockEvents) {
            if (expectedEvent.name().equals("name")) {
                assertRecyclerViewItem(
                        R.id.event_list,
                        p(R.id.content, expectedEvent.name()),
                        p(R.id.txtselection_date, "Waitlist closed"),
                        p(
                                R.id.txtDate,
                                EVENT_DATE_TIME_FORMATTER.format(
                                        expectedEvent.eventTime().toInstant())));
            } else {
                assertRecyclerViewItem(
                        R.id.event_list,
                        p(R.id.content, expectedEvent.name()),
                        p(
                                R.id.txtselection_date,
                                MessageFormat.format(
                                        "Selection date: {0}",
                                        SELECTION_DATE_FORMATTER.format(
                                                expectedEvent.selectionTime().toInstant()))),
                        p(
                                R.id.txtDate,
                                EVENT_DATE_TIME_FORMATTER.format(
                                        expectedEvent.eventTime().toInstant())));
            }
        }
        onView(withId(R.id.event_list)).check(matches(isDisplayed()));
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
    protected Class<BrowseEventsFragment> getFragmentClass() {
        return BrowseEventsFragment.class;
    }
}
