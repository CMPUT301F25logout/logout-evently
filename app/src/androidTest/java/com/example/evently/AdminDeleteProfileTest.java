package com.example.evently;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import android.os.Bundle;
import androidx.navigation.NavGraph;
import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.Timestamp;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.example.evently.data.AccountDB;
import com.example.evently.data.EventsDB;
import com.example.evently.data.generic.Promise;
import com.example.evently.data.model.Account;
import com.example.evently.data.model.Category;
import com.example.evently.data.model.Event;
import com.example.evently.ui.admin.ViewProfileDetailsFragment;

@RunWith(AndroidJUnit4.class)
public class AdminDeleteProfileTest extends EmulatedFragmentTest<ViewProfileDetailsFragment> {
    private static final AccountDB accountsDB = new AccountDB();
    private static final EventsDB eventsDB = new EventsDB();

    private static final Instant now = Instant.now();

    // Create a few mock accounts
    private static final Account testAccount =
            new Account("user@gmail.com", "user", Optional.empty(), "user@gmail.com");

    // Create a few events.
    private static final Event[] mockEvents = new Event[] {
        new Event(
                "name",
                "description",
                Category.EDUCATIONAL,
                false,
                new Timestamp(now.plus(Duration.ofMinutes(8))),
                new Timestamp(now.plus(Duration.ofMinutes(18))),
                testAccount.email(),
                50),
        new Event(
                "name1",
                "description1",
                Category.EDUCATIONAL,
                false,
                new Timestamp(now.plus(Duration.ofMinutes(1))),
                new Timestamp(now.plus(Duration.ofMinutes(11))),
                testAccount.email(),
                50),
        new Event(
                "name2",
                "description2",
                Category.EDUCATIONAL,
                false,
                new Timestamp(now.plus(Duration.ofMinutes(2))),
                new Timestamp(now.plus(Duration.ofMinutes(12))),
                testAccount.email(),
                50),
        new Event(
                "name3",
                "description3",
                Category.EDUCATIONAL,
                false,
                new Timestamp(now.minus(Duration.ofDays(20))),
                new Timestamp(now.minus(Duration.ofDays(18))),
                testAccount.email(),
                50)
    };

    @BeforeClass
    public static void setUpAccount() throws ExecutionException, InterruptedException {
        accountsDB.storeAccount(testAccount).await();

        for (int i = 0; i < mockEvents.length; i++) {
            eventsDB.storeEvent(mockEvents[i]).await();
        }
    }

    @Test
    public void testDeletingAccount() throws InterruptedException {
        Thread.sleep(2000);

        // Test to see if the name and email are displayed
        onView(withText("Name: " + testAccount.name())).check(matches(isDisplayed()));
        onView(withText("Email: " + testAccount.email())).check(matches(isDisplayed()));

        // Test to if the organizer made events
        eventsDB.fetchEventsByOrganizers(testAccount.email()).thenRun(events -> {
            assertFalse(events.isEmpty());
        });

        // Test if deleting the profile will remove the account from the database
        onView(withId(R.id.delete)).perform(ViewActions.click());
        onView(withId(R.id.confirm_button)).perform(click());

        accountsDB.fetchAccount(testAccount.email()).thenRun(account -> {
            assertTrue(account.isEmpty());
        });

        // Test if all created events by this user will also be deleted
        // TODO this will fail since cascading isn't implemented yet
        /*
        eventsDB.fetchEventsByOrganizers(testAccount.email()).thenRun(events ->{
            assertTrue(events.isEmpty());
        });
         */

        // TODO this will also fail since cascading isn't implemented yet
        //  (Not sure if this needs to be tested since AdminDeleteEventTest covers this)
        // Test if all entrants inside those events will also be deleted
        /*
        for (int i = 0; i < mockEvents.length; i++){
            eventsDB.fetchEventEntrants(mockEvents[i].eventID()).thenRun(entrants -> {
                assertTrue(entrants.isEmpty());
            });
        }
         */
    }

    @AfterClass
    public static void tearDownAccount() throws ExecutionException, InterruptedException {
        Promise.all(accountsDB.nuke(), eventsDB.nuke()).await();
    }

    @Override
    protected int getGraph() {
        return R.navigation.admin_graph;
    }

    @Override
    protected int getSelfDestination(NavGraph graph) {
        return R.id.profile_details;
    }

    @Override
    protected Bundle getSelfDestinationArgs() {
        final var bundle = new Bundle();
        bundle.putSerializable("accountEmail", testAccount.email());
        return bundle;
    }

    @Override
    protected Class<ViewProfileDetailsFragment> getFragmentClass() {
        return ViewProfileDetailsFragment.class;
    }
}
