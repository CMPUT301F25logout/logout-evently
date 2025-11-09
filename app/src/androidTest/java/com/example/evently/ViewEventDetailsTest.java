package com.example.evently;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.example.evently.MatcherUtils.assertRecyclerViewItem;
import static com.example.evently.MatcherUtils.p;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
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
import com.example.evently.data.generic.Promise;
import com.example.evently.data.model.Account;
import com.example.evently.data.model.Category;
import com.example.evently.data.model.Event;
import com.example.evently.ui.entrant.ViewEventDetailsFragment;

@RunWith(AndroidJUnit4.class)
public class ViewEventDetailsTest extends EmulatedFragmentTest<ViewEventDetailsFragment> {
    private static final EventsDB eventsDB = new EventsDB();

    private static final Instant now = Instant.now();
    // We can use the same times for these tests.
    private static final Timestamp selectionTime = new Timestamp(now.plus(Duration.ofMillis(2)));
    private static final Timestamp eventTime = new Timestamp(now.plus(Duration.ofMinutes(10)));

    private static final Event mockEvent = new Event(
            "name", "description", Category.EDUCATIONAL, selectionTime, eventTime, "orgEmail", 50);

    private static final Account[] extraAccounts = new Account[] {
        new Account("email@gmail.com", "User", Optional.empty(), "email@gmail.com"),
        new Account("email1@gmail.com", "User1", Optional.empty(), "email1@gmail.com"),
        new Account("email2@gmail.com", "User2", Optional.empty(), "email2@gmail.com"),
        new Account("email3@gmail.com", "User3", Optional.empty(), "email3@gmail.com"),
        new Account("email5@gmail.com", "User5", Optional.empty(), "email4@gmail.com"),
        new Account("email6@gmail.com", "User6", Optional.empty(), "email6@gmail.com")
    };

    @Override
    public List<Account> extraMockAccounts() {
        return Arrays.asList(extraAccounts);
    }

    @BeforeClass
    public static void setUpEventEnroll() throws ExecutionException, InterruptedException {
        // Store the events.
        eventsDB.storeEvent(mockEvent).await();

        // Enroll a few accounts into the event.
        for (int i = 0; i < extraAccounts.length; i++) {
            if (i % 2 == 0) {
                eventsDB.enroll(mockEvent.eventID(), extraAccounts[i].email()).await();
            }
        }
    }

    @AfterClass
    public static void tearDownEventEnroll() throws ExecutionException, InterruptedException {
        Promise.all(eventsDB.nuke()).await();
    }

    @Test
    public void testViewingEventDetails() throws InterruptedException {
        Thread.sleep(2000);

        onView(withText(mockEvent.description())).check(matches(isDisplayed()));

        Account[] expectedAccounts =
                new Account[] {extraAccounts[0], extraAccounts[2], extraAccounts[4]};

        // Test if the account's name show up on the recycler view
        for (final var expectedAccount : expectedAccounts) {
            assertRecyclerViewItem(
                    R.id.entrantListContainer, p(R.id.entrant_name, expectedAccount.name()));
        }
    }

    @Override
    protected int getGraph() {
        return R.navigation.entrant_graph;
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
    protected Class<ViewEventDetailsFragment> getFragmentClass() {
        return ViewEventDetailsFragment.class;
    }
}
