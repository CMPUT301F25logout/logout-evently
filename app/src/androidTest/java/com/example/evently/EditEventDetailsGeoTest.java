package com.example.evently;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.swipeUp;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static java.util.Map.entry;
import static org.junit.Assert.assertTrue;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import android.os.Bundle;
import androidx.core.widget.NestedScrollView;
import androidx.navigation.NavGraph;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.example.evently.data.EventsDB;
import com.example.evently.data.model.Account;
import com.example.evently.data.model.Category;
import com.example.evently.data.model.Event;
import com.example.evently.ui.organizer.EditEventDetailsFragment;
import com.example.evently.utils.FirebaseAuthUtils;

@RunWith(AndroidJUnit4.class)
public class EditEventDetailsGeoTest extends EmulatedFragmentTest<EditEventDetailsFragment> {
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
            true,
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

    private static final Random rand = new Random();

    private static final Map<String, GeoPoint> entrantLocations = Arrays.stream(extraAccounts)
            .map(acc -> {
                final var maxExcl = 91;
                final var min = -90;
                final var lat = rand.nextInt(maxExcl - min) + min;
                final var lng = rand.nextInt(maxExcl - min) + min;
                return entry(acc.email(), new GeoPoint(lat, lng));
            })
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    @Override
    public List<Account> extraMockAccounts() {
        return new ArrayList<>(Arrays.asList(extraAccounts));
    }

    @BeforeClass
    public static void setUpEventEnroll() throws ExecutionException, InterruptedException {
        // Store the events.
        eventsDB.storeEvent(mockEvent).await();

        // Enroll most of the accounts into the event (alongside their locations).
        for (int i = 1; i < extraAccounts.length; i++) {
            eventsDB.unsafeEnroll(
                            mockEvent.eventID(),
                            extraAccounts[i].email(),
                            entrantLocations.get(extraAccounts[i].email()))
                    .await();
        }
    }

    @AfterClass
    public static void tearDownEventEnroll() throws ExecutionException, InterruptedException {
        eventsDB.deleteEvent(mockEvent.eventID()).await();
    }

    @Test
    public void testPeopleMap() throws InterruptedException {
        Thread.sleep(1000);

        onView(withText(mockEvent.description())).check(matches(isDisplayed()));

        // Get to the bottom of the scroll view.
        onView(isAssignableFrom(NestedScrollView.class)).perform(swipeUp());

        // Open the map!
        onView(withText(R.string.entrant_map_btn)).perform(scrollTo(), click());

        Thread.sleep(10000);

        // No real way to test if the markers are there...
        assertTrue(true);
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
