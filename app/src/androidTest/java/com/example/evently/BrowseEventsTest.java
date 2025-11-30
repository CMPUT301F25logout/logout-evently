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
    // We can use the same times for these tests.\

    private static final DateTimeFormatter SELECTION_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault());
    private static final DateTimeFormatter EVENT_DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());
    private static final Duration EVENT_GAP = Duration.ofDays(2);
    private static final Timestamp[] selectionTimes = new Timestamp[] {
        startOfDayTimestamp(now.minusDays(1)),
        startOfDayTimestamp(now.plusDays(1)),
        startOfDayTimestamp(now.plusDays(2)),
        startOfDayTimestamp(now.plusDays(3)),
        startOfDayTimestamp(now.plusDays(4)),
        startOfDayTimestamp(now.plusDays(5)),
        startOfDayTimestamp(now.plusDays(6)),
        startOfDayTimestamp(now.plusDays(7)),
        startOfDayTimestamp(now.plusDays(8))
    };

    // Create a few events.
    private static final Event[] mockEvents = new Event[] {
        new Event(
                "name",
                "description",
                Category.EDUCATIONAL,
                selectionTimes[0],
                eventTimeAfter(selectionTimes[0]),
                "orgEmail",
                50),
        new Event(
                "name1",
                "description1",
                Category.EDUCATIONAL,
                selectionTimes[1],
                eventTimeAfter(selectionTimes[1]),
                "orgEmail",
                50),
        new Event(
                "name2",
                "description2",
                Category.EDUCATIONAL,
                selectionTimes[2],
                eventTimeAfter(selectionTimes[2]),
                "orgEmail",
                50),
        new Event(
                "name3",
                "description3",
                Category.EDUCATIONAL,
                selectionTimes[3],
                eventTimeAfter(selectionTimes[3]),
                "orgEmail",
                50),
        new Event(
                "name4",
                "description4",
                Category.EDUCATIONAL,
                selectionTimes[4],
                eventTimeAfter(selectionTimes[4]),
                "orgEmail",
                50),
        new Event(
                "name5",
                "description5",
                Category.EDUCATIONAL,
                selectionTimes[5],
                eventTimeAfter(selectionTimes[5]),
                "orgEmail",
                50),
        new Event(
                "name6",
                "description6",
                Category.EDUCATIONAL,
                selectionTimes[6],
                eventTimeAfter(selectionTimes[6]),
                "orgEmail",
                50),
        new Event(
                "name7",
                "description7",
                Category.EDUCATIONAL,
                selectionTimes[7],
                eventTimeAfter(selectionTimes[7]),
                "orgEmail",
                50),
        new Event(
                "name8",
                "description8",
                Category.EDUCATIONAL,
                selectionTimes[8],
                eventTimeAfter(selectionTimes[8]),
                "orgEmail",
                50)
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
