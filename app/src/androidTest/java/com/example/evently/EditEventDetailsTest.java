package com.example.evently;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.swipeUp;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasSibling;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.example.evently.MatcherUtils.assertRecyclerViewItem;
import static com.example.evently.MatcherUtils.p;
import static org.hamcrest.Matchers.allOf;
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
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.Timestamp;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import com.example.evently.data.EventsDB;
import com.example.evently.data.model.Account;
import com.example.evently.data.model.Category;
import com.example.evently.data.model.Event;
import com.example.evently.ui.organizer.EditEventDetailsFragment;
import com.example.evently.utils.FirebaseAuthUtils;

@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class EditEventDetailsTest extends EmulatedFragmentTest<EditEventDetailsFragment> {
    private static final EventsDB eventsDB = new EventsDB();

    private static final Instant now = Instant.now();
    // We can use the same times for these tests.
    private static final Timestamp selectionTime = new Timestamp(now.plus(Duration.ofMillis(2)));
    private static final Timestamp eventTime = new Timestamp(now.plus(Duration.ofMinutes(10)));

    private static final int selectionLimit = 4;

    private static final Event mockEvent = new Event(
            "name",
            "description",
            Category.Educational,
            false,
            selectionTime,
            eventTime,
            FirebaseAuthUtils.getCurrentEmail(),
            selectionLimit);

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

        // Enroll most of the accounts into the event.
        for (int i = 1; i < extraAccounts.length; i++) {
            eventsDB.unsafeEnroll(mockEvent.eventID(), extraAccounts[i].email()).await();
        }

        // Select a few of them (1, 2, 3, and 4)
        for (int i = 1; i <= selectionLimit; i++) {
            eventsDB.addSelected(mockEvent.eventID(), extraAccounts[i].email()).await();
        }

        // Some of them accept (2 and 3), the rest cancel (1 and 4)
        for (int i = 2; i <= selectionLimit - 1; i++) {
            eventsDB.addAccepted(mockEvent.eventID(), extraAccounts[i].email()).await();
        }
        eventsDB.addCancelled(mockEvent.eventID(), extraAccounts[1].email()).await();
        eventsDB.addCancelled(mockEvent.eventID(), extraAccounts[selectionLimit].email())
                .await();
    }

    @AfterClass
    public static void tearDownEventEnroll() throws ExecutionException, InterruptedException {
        eventsDB.deleteEvent(mockEvent.eventID()).await();
    }

    @Test
    public void test1_EnrolledPeople() throws InterruptedException {
        Thread.sleep(1000);

        onView(withText(mockEvent.description())).check(matches(isDisplayed()));

        // Get to the bottom of the scroll view.
        onView(isAssignableFrom(NestedScrollView.class)).perform(swipeUp());

        // Check the enrolled tab.
        onView(withText("Enrolled")).perform(click());

        // Test if the account's emails shows up on the recycler view
        for (int i = 1; i < extraAccounts.length; i++) {
            final var expectedAccount = extraAccounts[i];
            assertRecyclerViewItem(R.id.entrantList, p(R.id.entrant_name, expectedAccount.name()));
        }

        // Ensure unexpected account(s) do not show up in here.
        assertThrows(
                PerformException.class,
                () -> assertRecyclerViewItem(
                        R.id.entrantList, p(R.id.entrant_name, extraAccounts[0].name())));
    }

    @Test
    public void test2_SelectedPeople() throws InterruptedException {
        Thread.sleep(1000);

        onView(withText(mockEvent.description())).check(matches(isDisplayed()));

        // Get to the bottom of the scroll view.
        onView(isAssignableFrom(NestedScrollView.class)).perform(swipeUp());

        // Check the selected tab.
        onView(withText("Selected")).perform(click());

        // For whatever reason, it takes time for viewpager to get rid of the old tab.
        Thread.sleep(1000);

        // Test if the account's emails shows up on the recycler view
        for (int i = 1; i <= selectionLimit; i++) {
            final var expectedAccount = extraAccounts[i];
            assertRecyclerViewItem(R.id.entrantList, p(R.id.entrant_name, expectedAccount.name()));
        }

        // Ensure unexpected account(s) do not show up in here.
        for (int i = selectionLimit + 1; i < extraAccounts.length; i++) {
            final var expectedAccount = extraAccounts[i];
            assertThrows(
                    PerformException.class,
                    () -> assertRecyclerViewItem(
                            R.id.entrantList, p(R.id.entrant_name, expectedAccount.name())));
        }
    }

    @Test
    public void test3_AcceptedPeople() throws InterruptedException {
        Thread.sleep(1000);

        onView(withText(mockEvent.description())).check(matches(isDisplayed()));

        // Get to the bottom of the scroll view.
        onView(isAssignableFrom(NestedScrollView.class)).perform(swipeUp());

        // Check the accepted tab.
        onView(withText("Accepted")).perform(click());

        // For whatever reason, it takes time for viewpager to get rid of the old tab.
        Thread.sleep(1000);

        // Test if the account's emails shows up on the recycler view
        for (int i = 2; i <= selectionLimit - 1; i++) {
            final var expectedAccount = extraAccounts[i];
            assertRecyclerViewItem(R.id.entrantList, p(R.id.entrant_name, expectedAccount.name()));
        }

        // Ensure unexpected account(s) do not show up in here.
        assertThrows(
                PerformException.class,
                () -> assertRecyclerViewItem(
                        R.id.entrantList, p(R.id.entrant_name, extraAccounts[1].name())));
        assertThrows(
                PerformException.class,
                () -> assertRecyclerViewItem(
                        R.id.entrantList, p(R.id.entrant_name, extraAccounts[4].name())));
    }

    @Test
    public void test4_CancelledPeople() throws InterruptedException {
        Thread.sleep(1000);

        onView(withText(mockEvent.description())).check(matches(isDisplayed()));

        // Get to the bottom of the scroll view.
        onView(isAssignableFrom(NestedScrollView.class)).perform(swipeUp());

        // Check the cancelled tab.
        onView(withText("Cancelled")).perform(scrollTo(), click());

        // For whatever reason, it takes time for viewpager to get rid of the old view.
        Thread.sleep(1000);

        // Test if the account's emails shows up on the recycler view
        assertRecyclerViewItem(R.id.entrantList, p(R.id.entrant_name, extraAccounts[1].name()));
        assertRecyclerViewItem(R.id.entrantList, p(R.id.entrant_name, extraAccounts[4].name()));

        // Ensure unexpected account(s) do not show up in here.
        for (int i = 2; i <= selectionLimit - 1; i++) {
            final var expectedAccount = extraAccounts[i];
            assertThrows(
                    PerformException.class,
                    () -> assertRecyclerViewItem(
                            R.id.entrantList, p(R.id.entrant_name, expectedAccount.name())));
        }
    }

    @Test
    public void test5_CancelSelected() throws InterruptedException {
        Thread.sleep(2000);

        onView(withText(mockEvent.description())).check(matches(isDisplayed()));

        // Get to the bottom of the scroll view.
        onView(isAssignableFrom(NestedScrollView.class)).perform(swipeUp());

        // Check the selected tab.
        onView(withText("Selected")).perform(scrollTo(), click());

        // Wait for viewpager
        Thread.sleep(1000);

        String cancelingName = "user2";

        // Used to scroll to recycler view item
        assertRecyclerViewItem(R.id.entrantList, p(R.id.entrant_name, cancelingName));

        // The following line of code is from Google, Gemini 3 Pro:
        // "Using hasSibling, show me how to click on a button R.id.removeButton in a recycler view
        // next to a known email", 2025-11-30
        onView(allOf(withId(R.id.removeButton), hasSibling(withText(cancelingName)), isDisplayed()))
                .perform(click());

        // Waits for EventViewModel to update
        Thread.sleep(2000);

        // Ensure unexpected account(s) do not show up in here.
        assertThrows(
                PerformException.class,
                () -> assertRecyclerViewItem(
                        R.id.entrantList, p(R.id.entrant_name, cancelingName)));

        // Navigate to the cancelled tab.
        onView(withText("Cancelled")).perform(scrollTo(), click());

        // Wait for viewpager
        Thread.sleep(2000);

        // Asserts the canceled email is in the canceled tab
        assertRecyclerViewItem(R.id.entrantList, p(R.id.entrant_name, cancelingName));
    }

    @Test
    public void test6_NoMap() throws InterruptedException {
        Thread.sleep(1000);

        // Get to the bottom of the scroll view.
        onView(isAssignableFrom(NestedScrollView.class)).perform(swipeUp());

        // Ensure the map button is not displayed.
        onView(withId(R.id.open_map))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
    }

    @Override
    protected int getGraph() {
        return R.navigation.organizer_graph;
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
    protected Class<EditEventDetailsFragment> getFragmentClass() {
        return EditEventDetailsFragment.class;
    }
}
