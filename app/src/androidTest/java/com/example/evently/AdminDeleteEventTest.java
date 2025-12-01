package com.example.evently;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertTrue;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import android.os.Bundle;
import androidx.navigation.NavGraph;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.Timestamp;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.example.evently.data.EventsDB;
import com.example.evently.data.model.Account;
import com.example.evently.data.model.Category;
import com.example.evently.data.model.Event;
import com.example.evently.ui.admin.AdminEventDetailsFragment;

@RunWith(AndroidJUnit4.class)
/**
 * Test for deleting an event from the admin view of the browse events
 */
public class AdminDeleteEventTest extends EmulatedFragmentTest<AdminEventDetailsFragment> {
    private static final EventsDB eventsDB = new EventsDB();

    private static final Instant now = Instant.now();
    // We can use the same times for these tests.
    private static final Timestamp selectionTime = new Timestamp(now.plus(Duration.ofMillis(2)));
    private static final Timestamp eventTime = new Timestamp(now.plus(Duration.ofMinutes(10)));

    private static final Event mockEvent = new Event(
            "name",
            "description",
            Category.Educational,
            false,
            selectionTime,
            eventTime,
            "orgEmail",
            50);

    private static final Account[] extraAccounts = new Account[] {
        new Account("email@gmail.com", "User", Optional.empty(), "email@gmail.com"),
        new Account("email1@gmail.com", "User1", Optional.empty(), "email1@gmail.com"),
        new Account("email2@gmail.com", "User2", Optional.empty(), "email2@gmail.com"),
        new Account("email3@gmail.com", "User3", Optional.empty(), "email3@gmail.com"),
        new Account("email4@gmail.com", "User4", Optional.empty(), "email4@gmail.com"),
        new Account("email5@gmail.com", "User5", Optional.empty(), "email5@gmail.com"),
        new Account("email6@gmail.com", "User6", Optional.empty(), "email6@gmail.com"),
        new Account("email7@gmail.com", "User7", Optional.empty(), "email7@gmail.com")
    };

    @BeforeClass
    public static void setUpEventEnroll() throws ExecutionException, InterruptedException {
        // Store the event.
        eventsDB.storeEvent(mockEvent).await();

        // Enroll a few accounts into the event.
        for (int i = 0; i < extraAccounts.length; i++) {
            if (i != extraAccounts.length - 1) {
                eventsDB.unsafeEnroll(mockEvent.eventID(), extraAccounts[i].email())
                        .await();
            }
        }

        // Add a few to selected
        eventsDB.addSelected(mockEvent.eventID(), extraAccounts[0].email()).await();
        eventsDB.addSelected(mockEvent.eventID(), extraAccounts[2].email()).await();
        eventsDB.addSelected(mockEvent.eventID(), extraAccounts[4].email()).await();

        // Add a few to accepted
        eventsDB.addAccepted(mockEvent.eventID(), extraAccounts[1].email()).await();
        eventsDB.addAccepted(mockEvent.eventID(), extraAccounts[3].email()).await();

        // Add a few to cancelled
        eventsDB.addCancelled(mockEvent.eventID(), extraAccounts[5].email()).await();
        eventsDB.addCancelled(mockEvent.eventID(), extraAccounts[6].email()).await();
    }

    @Test
    public void testDeleteEvent() throws InterruptedException {
        Thread.sleep(2000);

        onView(withText(mockEvent.description())).check(matches(isDisplayed()));

        // Test if deleting the event will remove the event and entrants from the database
        onView(withId(R.id.removeEvent)).perform(scrollTo(), click());
        onView(withId(R.id.confirm_button)).perform(click());

        eventsDB.fetchEvent(mockEvent.eventID()).thenRun(event -> {
            assertTrue(event.isEmpty());
        });
        eventsDB.fetchEventEntrants(mockEvent.eventID()).thenRun(entrants -> {
            assertTrue(entrants.isEmpty());
        });
    }

    @AfterClass
    public static void tearDownEventEnroll() throws ExecutionException, InterruptedException {
        eventsDB.deleteEvent(mockEvent.eventID()).await();
    }

    @Override
    protected int getSelfDestination(NavGraph graph) {
        return R.id.event_details;
    }

    @Override
    protected Bundle getSelfDestinationArgs() {
        final var bundle = new Bundle();
        bundle.putSerializable("eventID", mockEvent.eventID());
        return bundle;
    }

    @Override
    protected int getGraph() {
        return R.navigation.admin_graph;
    }

    @Override
    protected Class<AdminEventDetailsFragment> getFragmentClass() {
        return AdminEventDetailsFragment.class;
    }
}
