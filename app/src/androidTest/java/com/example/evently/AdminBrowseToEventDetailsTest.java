package com.example.evently;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static com.example.evently.MatcherUtils.assertRecyclerViewItem;
import static com.example.evently.MatcherUtils.p;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutionException;

import androidx.navigation.fragment.NavHostFragment;
import androidx.test.espresso.action.ViewActions;
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
import com.example.evently.ui.admin.AdminBrowseEventsFragment;

/**
 * Test switching to the admin view of the event details from the admin view of the browse events
 */
@RunWith(AndroidJUnit4.class)
public class AdminBrowseToEventDetailsTest extends EmulatedFragmentTest<AdminBrowseEventsFragment> {
    private static final EventsDB eventsDB = new EventsDB();

    private static final Instant now = Instant.now();
    // We can use the same times for these tests.
    private static final Timestamp selectionTime = new Timestamp(now.plus(Duration.ofMillis(100)));
    private static final Timestamp eventTime = new Timestamp(now.plus(Duration.ofMinutes(10)));
    private static final DateTimeFormatter EVENT_DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    // Create a few events.
    private static final Event[] mockEvents = new Event[] {
        new Event(
                "name",
                "description",
                Category.EDUCATIONAL,
                false,
                selectionTime,
                eventTime,
                "orgEmail",
                50)
    };

    @BeforeClass
    public static void storeEvents() throws ExecutionException, InterruptedException {
        eventsDB.storeEvent(mockEvents[0]).await();
    }

    // Test event details button
    @Test
    public void testSwitchingToEventDetails() throws InterruptedException {
        Thread.sleep(2000);

        Event expectedEvent = mockEvents[0];

        assertRecyclerViewItem(
                R.id.event_list,
                p(R.id.content, expectedEvent.name()),
                p(R.id.txtselection_date, "Waitlist closed"),
                p(
                        R.id.txtDate,
                        EVENT_DATE_TIME_FORMATTER.format(
                                expectedEvent.eventTime().toInstant())));

        // Test if pressing the event details button navigates to event_details
        onView(withId(R.id.btnDetails)).perform(ViewActions.click());
        scenario.onFragment(fragment -> {
            final var dest = NavHostFragment.findNavController(fragment).getCurrentDestination();
            assertNotNull(dest);
            assertEquals(dest.getId(), R.id.event_details);
        });
    }

    @AfterClass
    public static void tearDownEvents() throws ExecutionException, InterruptedException {
        Promise.all(eventsDB.nuke()).await();
    }

    @Override
    protected int getGraph() {
        return R.navigation.admin_graph;
    }

    @Override
    protected Class<AdminBrowseEventsFragment> getFragmentClass() {
        return AdminBrowseEventsFragment.class;
    }
}
