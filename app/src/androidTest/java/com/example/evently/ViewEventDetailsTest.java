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
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThrows;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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
import androidx.test.filters.LargeTest;

import com.google.firebase.Timestamp;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.example.evently.data.AccountDB;
import com.example.evently.data.EventsDB;
import com.example.evently.data.model.Account;
import com.example.evently.data.model.Category;
import com.example.evently.data.model.Event;
import com.example.evently.ui.entrant.ViewEventDetailsFragment;

@RunWith(AndroidJUnit4.class)
/**
 * Tests for the view event details fragment
 */
public class ViewEventDetailsTest extends EmulatedFragmentTest<ViewEventDetailsFragment> {
    private static final EventsDB eventsDB = new EventsDB();

    private static final Instant now = Instant.now();
    // We can use the same times for these tests.
    private static final Timestamp selectionTime = new Timestamp(now.plus(Duration.ofMillis(2)));
    private static final Timestamp eventTime = new Timestamp(now.plus(Duration.ofMinutes(10)));

    private static final DateTimeFormatter EVENT_DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    private static final Event mockEvent = new Event(
            "name",
            "description",
            Category.Educational,
            false,
            selectionTime,
            eventTime,
            defaultMockAccount.email(),
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
        // Store the events, and the organizer
        eventsDB.storeEvent(mockEvent)
                .alongside(new AccountDB().storeAccount(defaultMockAccount))
                .await();

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

    // Note: Not run in CI because it has proven impossible to get this to pass there.
    @LargeTest
    @Test
    public void testSelectionDetailsButtonOpensDialog() throws InterruptedException {
        Thread.sleep(2000);

        // Get to the bottom of the scroll view.
        onView(withId(R.id.lotteryGuidelinesButton)).perform(scrollTo(), click());
        onView(withText(R.string.lottery_guidelines_dialog_title)).check(matches(isDisplayed()));
        onView(withText(R.string.lottery_guidelines_dialog_message)).check(matches(isDisplayed()));
        onView(withText(R.string.lottery_guidelines_dialog_positive)).perform(click());
    }

    @Test
    public void testDateFormatting() throws InterruptedException {
        Thread.sleep(2000);

        onView(withText(mockEvent.description())).check(matches(isDisplayed()));

        // Asserts the correct selection time is shown
        String formattedDate =
                EVENT_DATE_TIME_FORMATTER.format(mockEvent.selectionTime().toInstant());
        onView(allOf(withId(R.id.selectionDateText), withText(formattedDate)))
                .check(matches(isDisplayed()));

        // Asserts the correct event time is shown
        formattedDate = EVENT_DATE_TIME_FORMATTER.format(mockEvent.eventTime().toInstant());
        onView(allOf(withId(R.id.eventDateTime), withText(formattedDate)))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testEntrantLimit() throws InterruptedException {
        Thread.sleep(2000);

        // Because the entrant list is not limited, the waitlist separator, and limit should not be
        // shown.
        onView(withId(R.id.waitlist_separator)).check(matches(not(isDisplayed())));
        onView(withId(R.id.entrantLimit)).check(matches(not(isDisplayed())));

        // Asserts 3 is shown as the number of entrants
        onView(withId(R.id.currentEntrantCount)).check(matches(withText("3")));

        // Asserts the correct selection time is shown
        String formattedDate =
                EVENT_DATE_TIME_FORMATTER.format(mockEvent.selectionTime().toInstant());
        onView(withText(formattedDate)).check(matches(isDisplayed()));
    }

    @Test
    public void testSeatLimit() throws InterruptedException {
        Thread.sleep(2000);

        // Asserts correct seat limit is shown
        String seatLimit = String.valueOf(mockEvent.selectionLimit());
        onView(withId(R.id.seatsText)).check(matches(withText(seatLimit)));
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
