package com.example.evently;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.example.evently.MatcherUtils.assertRecyclerViewItem;
import static com.example.evently.MatcherUtils.p;
import static org.junit.Assert.assertEquals;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutionException;

import androidx.test.espresso.action.ViewActions;
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
import com.example.evently.ui.entrant.BrowseEventsFragment;

/**
 * Test switching to the event details
 * @author Vinson Lou
 */
@RunWith(AndroidJUnit4.class)
public class SwitchToEventDetailsTest extends EmulatedFragmentTest<BrowseEventsFragment> {
    private static final EventsDB eventsDB = new EventsDB();

    private static final Instant now = Instant.now();
    // We can use the same times for these tests.
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
                50)
    };

    @BeforeClass
    public static void storeEvents() throws ExecutionException, InterruptedException {
        final var self = FirebaseEmulatorTest.mockAccount.email();
        eventsDB.storeEvent(mockEvents[0]).await();
        eventsDB.enroll(mockEvents[0].eventID(), self).await();
    }

    @Test
    public void testSwitchingToEventDetails() throws InterruptedException {
        Thread.sleep(2000);
        final DateTimeFormatter some_date =
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("UTC"));

        Event expectedEvent = mockEvents[0];

        assertRecyclerViewItem(
                R.id.event_list,
                p(R.id.content, expectedEvent.name()),
                p(R.id.txtselection_date, "Waitlist closed"),
                p(R.id.txtDate, some_date.format(expectedEvent.eventTime().toInstant())));

        onView(ViewMatchers.withId(R.id.btnDetails)).perform(ViewActions.click());
        assertEquals(navController.getCurrentDestination().getId(), R.id.fragment_event_details);
    }

    // Test event details button
    @Test
    public void testSwitchingToEventDetailsAndBack()
            throws ExecutionException, InterruptedException {
        Thread.sleep(2000);
        final DateTimeFormatter some_date =
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("UTC"));

        Event expectedEvent = mockEvents[0];

        assertRecyclerViewItem(
                R.id.event_list,
                p(R.id.content, expectedEvent.name()),
                p(R.id.txtselection_date, "Waitlist closed"),
                p(R.id.txtDate, some_date.format(expectedEvent.eventTime().toInstant())));

        onView(ViewMatchers.withId(R.id.btnDetails)).perform(ViewActions.click());
        assertEquals(navController.getCurrentDestination().getId(), R.id.fragment_event_details);

        onView(withText(mockEvents[0].description())).check(matches(isDisplayed()));

        onView(withId(R.id.buttonBack)).perform(click());

        assertRecyclerViewItem(
                R.id.event_list,
                p(R.id.content, expectedEvent.name()),
                p(R.id.txtselection_date, "Waitlist closed"),
                p(R.id.txtDate, some_date.format(expectedEvent.eventTime().toInstant())));
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
