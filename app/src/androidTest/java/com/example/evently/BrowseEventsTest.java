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
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutionException;

import androidx.test.espresso.contrib.RecyclerViewActions;
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

    private static final LocalDate now = LocalDate.now();
    // We can use the same times for these tests (for now).

    private static final DateTimeFormatter SELECTION_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault());
    private static final DateTimeFormatter EVENT_DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());
    private static final Duration EVENT_GAP = Duration.ofDays(2);
    private static final Timestamp[] selectionTimes = new Timestamp[] {
        startOfDayTimestamp(now.plusDays(1)),
        startOfDayTimestamp(now.plusDays(2)),
        startOfDayTimestamp(now.plusDays(3)),
    };

    // Create a few events.
    // Note: some events have been removed from here due to them not fitting on screen
    // and recyclerview scroll action not bringing them fully into view.
    private static final Event[] mockEvents = new Event[] {
        new Event(
                "name1",
                "description1",
                Category.Educational,
                false,
                selectionTimes[0],
                eventTimeAfter(selectionTimes[0]),
                "orgEmail",
                50),
        new Event(
                "name2",
                "description2",
                Category.Educational,
                false,
                selectionTimes[1],
                eventTimeAfter(selectionTimes[1]),
                "orgEmail",
                50),
        new Event(
                "name3",
                "description3",
                Category.Educational,
                false,
                selectionTimes[2],
                eventTimeAfter(selectionTimes[2]),
                "orgEmail",
                50),
    };

    private static Timestamp startOfDayTimestamp(LocalDate date) {
        return new Timestamp(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private static Timestamp eventTimeAfter(Timestamp selectionTime) {
        Instant selectionInstant =
                Instant.ofEpochSecond(selectionTime.getSeconds(), selectionTime.getNanoseconds());
        return new Timestamp(selectionInstant.plus(EVENT_GAP));
    }

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
    public static void removeEvents() throws ExecutionException, InterruptedException {
        for (final var mockEvent : mockEvents) {
            eventsDB.deleteEvent(mockEvent.eventID()).await();
        }
    }

    @Test
    public void testViewingEvents() throws InterruptedException {
        Thread.sleep(2000);

        // Test if each event added shows up on the recyclerview, one of the events is closed
        for (int i = 1; i < mockEvents.length; i++) {
            final var expectedEvent = mockEvents[i];

            onView(withId(R.id.event_list)).perform(RecyclerViewActions.scrollToPosition(i));
            assertRecyclerViewItem(
                    R.id.event_list,
                    p(R.id.content, expectedEvent.name()),
                    p(
                            R.id.txtselectionDate,
                            MessageFormat.format(
                                    "Selection date: {0}",
                                    SELECTION_DATE_FORMATTER.format(
                                            expectedEvent.selectionTime().toInstant()))),
                    p(
                            R.id.txtDate,
                            EVENT_DATE_TIME_FORMATTER.format(
                                    expectedEvent.eventTime().toInstant())));
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
