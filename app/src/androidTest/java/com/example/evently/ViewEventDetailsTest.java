package com.example.evently;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.swipeUp;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.example.evently.MatcherUtils.assertRecyclerViewItem;
import static com.example.evently.MatcherUtils.p;
import static org.junit.Assert.assertThrows;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import android.os.Bundle;
import androidx.core.widget.NestedScrollView;
import androidx.navigation.NavGraph;
import androidx.test.espresso.PerformException;
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
import com.example.evently.ui.entrant.ViewEventDetailsFragment;

@RunWith(AndroidJUnit4.class)
public class ViewEventDetailsTest extends EmulatedFragmentTest<ViewEventDetailsFragment> {
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
        new Account("email5@gmail.com", "User5", Optional.empty(), "email4@gmail.com"),
        new Account("email6@gmail.com", "User6", Optional.empty(), "email6@gmail.com")
    };

    @Override
    public List<Account> extraMockAccounts() {
        return new ArrayList<>(Arrays.asList(extraAccounts));
    }

    @BeforeClass
    public static void setUpEventEnroll() throws ExecutionException, InterruptedException {
        // Store the events.
        eventsDB.storeEvent(mockEvent).await();

        // Enroll a few accounts into the event.
        for (int i = 0; i < extraAccounts.length; i++) {
            if (i % 2 == 0) {
                eventsDB.unsafeEnroll(mockEvent.eventID(), extraAccounts[i].email())
                        .await();
            }
        }
    }

    @AfterClass
    public static void tearDownEventEnroll() throws ExecutionException, InterruptedException {
        eventsDB.deleteEvent(mockEvent.eventID()).await();
    }

    @Test
    public void testViewingEventDetails() throws InterruptedException {
        Thread.sleep(2000);

        onView(withText(mockEvent.description())).check(matches(isDisplayed()));

        Account[] expectedAccounts =
                new Account[] {extraAccounts[0], extraAccounts[2], extraAccounts[4]};

        // Get to the bottom of the scroll view.
        onView(isAssignableFrom(NestedScrollView.class)).perform(swipeUp());

        // Test if the account's emails shows up on the recycler view
        for (final var expectedAccount : expectedAccounts) {
            assertRecyclerViewItem(R.id.entrantList, p(R.id.entrant_name, expectedAccount.email()));
        }

        // Ensure unexpected account(s) do not show up in here.
        for (int i = 0; i < extraAccounts.length; i++) {
            if (i % 2 == 0) continue;
            final var unexpectedAccount = extraAccounts[i];
            assertThrows(
                    PerformException.class,
                    () -> assertRecyclerViewItem(
                            R.id.entrantList, p(R.id.entrant_name, unexpectedAccount.email())));
        }
    }

    @Test
    public void testSelectionDetailsButtonOpensDialog() throws InterruptedException {
        Thread.sleep(2000);

        // Get to the bottom of the scroll view.
        onView(withId(R.id.lotteryGuidelinesButton)).perform(scrollTo());
        onView(withId(R.id.lotteryGuidelinesButton)).check(matches(isDisplayed()));
        onView(withId(R.id.lotteryGuidelinesButton)).perform(click());
        onView(withText(R.string.lottery_guidelines_dialog_title)).check(matches(isDisplayed()));
        onView(withText(R.string.lottery_guidelines_dialog_message)).check(matches(isDisplayed()));
        onView(withText(R.string.lottery_guidelines_dialog_positive)).perform(click());
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
