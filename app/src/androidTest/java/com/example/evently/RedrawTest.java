package com.example.evently;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

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

@RunWith(AndroidJUnit4.class)
public class RedrawTest extends FirebaseEmulatorTest {
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
                3),
        new Event(
                "name",
                "description",
                Category.EDUCATIONAL,
                selectionTime,
                eventTime,
                "orgEmail",
                5)
    };

    @BeforeClass
    public static void setUpEnrolls() throws ExecutionException, InterruptedException {
        // TODO (chase): We need batch writes. No reason for there to be so many independent writes.
        // Store events into DB.
        Promise.all(Arrays.stream(mockEvents).map(eventsDB::storeEvent)).await();

        // Enroll a few users into these.
        // Since this not a full on UI test, we can use some fake accounts that don't even exist.
        Promise.all(
                        // 5 entries for event 0.
                        eventsDB.enroll(mockEvents[0].eventID(), "foo@bar.com"),
                        eventsDB.enroll(mockEvents[0].eventID(), "bar@bar.com"),
                        eventsDB.enroll(mockEvents[0].eventID(), "baz@bar.com"),
                        eventsDB.enroll(mockEvents[0].eventID(), "lorem@bar.com"),
                        eventsDB.enroll(mockEvents[0].eventID(), "ipsum@bar.com"),
                        // 6 entries for event 1.
                        eventsDB.enroll(mockEvents[1].eventID(), "foo@bar.com"),
                        eventsDB.enroll(mockEvents[1].eventID(), "bar@bar.com"),
                        eventsDB.enroll(mockEvents[1].eventID(), "baz@bar.com"),
                        eventsDB.enroll(mockEvents[1].eventID(), "lorem@bar.com"),
                        eventsDB.enroll(mockEvents[1].eventID(), "ipsum@bar.com"),
                        eventsDB.enroll(mockEvents[1].eventID(), "dolor@bar.com"))
                .await();

        Promise.all(
                        // Select 3 people.
                        eventsDB.addSelected(mockEvents[0].eventID(), "foo@bar.com"),
                        eventsDB.addSelected(mockEvents[0].eventID(), "baz@bar.com"),
                        eventsDB.addSelected(mockEvents[0].eventID(), "lorem@bar.com"),
                        // Select 5 people.
                        eventsDB.addSelected(mockEvents[1].eventID(), "foo@bar.com"),
                        eventsDB.addSelected(mockEvents[1].eventID(), "bar@bar.com"),
                        eventsDB.addSelected(mockEvents[1].eventID(), "lorem@bar.com"),
                        eventsDB.addSelected(mockEvents[1].eventID(), "ipsum@bar.com"),
                        eventsDB.addSelected(mockEvents[1].eventID(), "dolor@bar.com"))
                .await();
    }

    @AfterClass
    public static void tearDownNotifications() throws ExecutionException, InterruptedException {
        Promise.all(eventsDB.nuke()).await();
    }

    @Test
    public void expectFullRedraw_event0() throws InterruptedException, ExecutionException {
        final var canceller = "baz@bar.com";
        final var targetEventID = mockEvents[0].eventID();

        // Make one of the winners cancel for event 0.
        eventsDB.addCancelled(targetEventID, canceller).await();

        // The function should fire, and should select ONE of the remaining entrants.
        // Might have to wait a bit for the function to fire;
        Thread.sleep(5000);
        final var entrantsInfo =
                eventsDB.fetchEventEntrants(List.of(targetEventID)).await().get(0);

        // Cancelled user should be in the cancelled list.
        assertTrue(entrantsInfo.cancelled().contains(canceller));
        // They should be removed from the winners list.
        assertFalse(entrantsInfo.selected().contains(canceller));
        // One of the losers should be added to the winners list now.
        final var losers = List.of("bar@bar.com", "ipsum@bar.com");
        assertTrue(entrantsInfo.selected().stream().anyMatch(losers::contains));
    }

    @Test
    public void expectTrivialRedraw_event1() throws InterruptedException, ExecutionException {
        final var canceller = "lorem@bar.com";
        final var targetEventID = mockEvents[1].eventID();

        // Make one of the winners cancel for event 0.
        eventsDB.addCancelled(targetEventID, canceller).await();

        // The function should fire, and should select ONE of the remaining entrants.
        // Might have to wait a bit for the function to fire;
        Thread.sleep(5000);
        final var entrantsInfo =
                eventsDB.fetchEventEntrants(List.of(targetEventID)).await().get(0);

        // Cancelled user should be in the cancelled list.
        assertTrue(entrantsInfo.cancelled().contains(canceller));
        // They should be removed from the winners list.
        assertFalse(entrantsInfo.selected().contains(canceller));
        // There is only one loser; should be trivially added!
        assertTrue(entrantsInfo.selected().contains("baz@bar.com"));
    }
}
