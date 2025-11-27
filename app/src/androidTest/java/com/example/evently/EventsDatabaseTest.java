package com.example.evently;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.evently.data.generic.Promise;
import com.example.evently.utils.FirebaseAuthUtils;
import com.google.firebase.Timestamp;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.example.evently.data.EventsDB;
import com.example.evently.data.model.Category;
import com.example.evently.data.model.Event;
import com.google.firebase.firestore.GeoPoint;

@RunWith(AndroidJUnit4.class)
public class EventsDatabaseTest extends FirebaseEmulatorTest {
    /**
     * Creates an event for testing
     * @return created event
     */
    private Event testEvent() {
        return testEvent(false);
    }

    /**
     * Creates an event for testing
     * @param requiresLocation Whether or not the event should require location to enroll.
     * @return created event
     */
    private Event testEvent(boolean requiresLocation) {
        return testEvent(0, requiresLocation);
    }

    /**
     * Creates an event with values altered by num
     * @param num an integer value to include in values
     * @return created event
     */
    private Event testEvent(int num) {
        return testEvent(num, false);
    }

    /**
     * Creates an event with values altered by num
     * @param num an integer value to include in values
     * @param requiresLocation Whether or not the event should require location to enroll.
     * @return created event
     */
    private Event testEvent(int num, boolean requiresLocation) {
        return new Event(
                "testEvent" + num,
                "Event " + num + " created for testing",
                Category.EDUCATIONAL,
                requiresLocation,
                new Timestamp(LocalDate.of(2026, 10, 1)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()),
                new Timestamp(LocalDate.of(2027 + num, 1, 1)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()),
                "testOrganizer" + num + "@example.com",
                10L,
                50L);
    }

    /**
     * Tests the store, and fetch event operations.
     */
    @Test
    public void testStoreAndFetchEvent() throws InterruptedException, ExecutionException {
        EventsDB db = new EventsDB();

        Event event = testEvent();
        db.storeEvent(event).await();

        var fetchedEvent = db.fetchEvent(event.eventID()).await();
        assertTrue(fetchedEvent.isPresent());
        assertEquals(fetchedEvent.get().toHashMap(), event.toHashMap());
    }

    /**
     * Tests the store, and fetch event with geolocation.
     */
    @Test
    public void testStoreAndFetchGeolocationEvent() throws InterruptedException, ExecutionException {
        EventsDB db = new EventsDB();

        Event event = testEvent(true);
        db.storeEvent(event).await();

        final var fetchedEventOpt = db.fetchEvent(event.eventID()).await();
        assertTrue(fetchedEventOpt.isPresent());
        final var fetchedEvent = fetchedEventOpt.get();
        assertTrue(fetchedEvent.requiresLocation());
        assertEquals(fetchedEvent.toHashMap(), event.toHashMap());
    }

    @Test
    public void testEnrollGeolocationEvent() throws InterruptedException, ExecutionException {
        EventsDB db = new EventsDB();

        Event event = testEvent(true);
        db.storeEvent(event).await();

        // A couple sample accounts with locations.
        final var entrantLocations = new HashMap<String, GeoPoint>();
        entrantLocations.put("foo@bar.com", new GeoPoint(79, 82));
        entrantLocations.put(FirebaseAuthUtils.getCurrentEmail(), new GeoPoint(42, 42));
        entrantLocations.put("baz@foo.com", new GeoPoint(8947, 1029));
        entrantLocations.put("me@example.net", new GeoPoint(42, 42));

        // Enroll them all with the location.
        final var proms = new ArrayList<Promise<Void>>();
        entrantLocations.forEach((email, location) ->
            proms.add(db.enroll(event.eventID(), email, location))
        );
        Promise.all(proms.stream()).await();

        // Make sure the locations are properly available.
        final var entrantsInfoOpt = db.fetchEventEntrants(event.eventID()).await();
        assertTrue("Event entrants must be present in DB", entrantsInfoOpt.isPresent());
        final var entrantsInfo = entrantsInfoOpt.get();
        entrantLocations.forEach((email, location) ->
            assertEquals("Stored location must match", entrantsInfo.locations().get(email), location)
        );
    }

    /**
     * Test for the deleteEvent function
     */
    @Test
    public void testDeleteEvent() throws InterruptedException, ExecutionException {
        EventsDB db = new EventsDB();

        Event event = testEvent();
        db.storeEvent(event).await();

        db.deleteEvent(event.eventID()).await();

        var optionalEvent = db.fetchEvent(event.eventID()).await();
        assertFalse(optionalEvent.isPresent());
    }

    /**
     * Tests fetching events by organizer
     */
    @Test
    public void testFetchEventByOrganizer() throws InterruptedException, ExecutionException {
        EventsDB db = new EventsDB();

        Event event1 = testEvent(1);
        Event event2 = testEvent(2);
        db.storeEvent(event1).await();
        db.storeEvent(event2).await();

        var eventCollection = db.fetchEventsByOrganizers(event1.organizer()).await();
        assertTrue(eventCollection.contains(event1));
        assertFalse(eventCollection.contains(event2));

        eventCollection = db.fetchEventsByOrganizers("notAnOrganizer").await();
        assertTrue(eventCollection.isEmpty());
    }

    /**
     * Tests fetching events by date
     */
    @Test
    public void testFetchEventByDate() throws InterruptedException, ExecutionException {
        Instant timeCheck = LocalDate.of(2028, 2, 1)
                .atStartOfDay()
                .atZone(ZoneId.systemDefault())
                .toInstant();

        EventsDB db = new EventsDB();

        Event event1 = testEvent(1);
        Event event2 = testEvent(2);
        db.storeEvent(event1).await();
        db.storeEvent(event2).await();

        var eventCollection =
                db.fetchEventsByDate(new Timestamp(timeCheck), true).await();
        assertFalse(eventCollection.contains(event1));
        assertTrue(eventCollection.contains(event2));

        eventCollection = db.fetchEventsByDate(new Timestamp(timeCheck), false).await();

        assertTrue(eventCollection.contains(event1));
        assertFalse(eventCollection.contains(event2));
    }

    /**
     * Tests that users are removed from all lists of entrants, and non-inputted users are not affected
     */
    @Test
    public void testRemoveUserFromEvents() throws ExecutionException, InterruptedException {
        EventsDB db = new EventsDB();

        Event event1 = testEvent(1);
        Event event2 = testEvent(2);
        String account1 = "test1@test.com";
        String account2 = "test2@test.com";
        String account3 = "test3@test.com";
        String account4 = "test4@test.com";

        db.storeEvent(event1).await();
        db.storeEvent(event2).await();

        UUID id1 = event1.eventID();
        UUID id2 = event2.eventID();

        db.unsafeEnroll(id1, account1).await();
        db.addSelected(id1, account2).await();
        db.addCancelled(id1, account3).await();
        db.addAccepted(id1, account4).await();
        db.unsafeEnroll(id2, account1).await();
        db.addSelected(id2, account2).await();
        db.addCancelled(id2, account3).await();
        db.addAccepted(id2, account4).await();

        db.removeUserFromEvents("not@present.com").await();

        db.fetchEventEntrants(id1).thenRun(entrants -> {
            assertTrue(entrants.isPresent());
            assertTrue(entrants.get().all().contains(account1));
            assertTrue(entrants.get().selected().contains(account2));
            assertTrue(entrants.get().cancelled().contains(account3));
            assertTrue(entrants.get().accepted().contains(account4));
        });

        db.fetchEventEntrants(id2).thenRun(entrants -> {
            assertTrue(entrants.isPresent());
            assertTrue(entrants.get().all().contains(account1));
            assertTrue(entrants.get().selected().contains(account2));
            assertTrue(entrants.get().cancelled().contains(account3));
            assertTrue(entrants.get().accepted().contains(account4));
        });

        db.removeUserFromEvents(account1).await();

        db.fetchEventEntrants(id1).thenRun(entrants -> {
            assertTrue(entrants.isPresent());
            assertFalse(entrants.get().all().contains(account1));
            assertTrue(entrants.get().selected().contains(account2));
            assertTrue(entrants.get().cancelled().contains(account3));
            assertTrue(entrants.get().accepted().contains(account4));
        });

        db.fetchEventEntrants(id2).thenRun(entrants -> {
            assertTrue(entrants.isPresent());
            assertFalse(entrants.get().all().contains(account1));
            assertTrue(entrants.get().selected().contains(account2));
            assertTrue(entrants.get().cancelled().contains(account3));
            assertTrue(entrants.get().accepted().contains(account4));
        });

        db.removeUserFromEvents(account2).await();

        db.fetchEventEntrants(id1).thenRun(entrants -> {
            assertTrue(entrants.isPresent());
            assertFalse(entrants.get().selected().contains(account2));
        });

        db.fetchEventEntrants(id2).thenRun(entrants -> {
            assertTrue(entrants.isPresent());
            assertFalse(entrants.get().selected().contains(account2));
        });

        db.removeUserFromEvents(account3).await();

        db.fetchEventEntrants(id1).thenRun(entrants -> {
            assertTrue(entrants.isPresent());
            assertFalse(entrants.get().cancelled().contains(account3));
        });

        db.fetchEventEntrants(id2).thenRun(entrants -> {
            assertTrue(entrants.isPresent());
            assertFalse(entrants.get().cancelled().contains(account3));
        });

        db.removeUserFromEvents(account4).await();

        db.fetchEventEntrants(id1).thenRun(entrants -> {
            assertTrue(entrants.isPresent());
            assertEquals(0, entrants.get().all().size());
            assertEquals(0, entrants.get().selected().size());
            assertEquals(0, entrants.get().cancelled().size());
            assertEquals(0, entrants.get().accepted().size());
        });

        db.fetchEventEntrants(id2).thenRun(entrants -> {
            assertTrue(entrants.isPresent());
            assertEquals(0, entrants.get().all().size());
            assertEquals(0, entrants.get().selected().size());
            assertEquals(0, entrants.get().cancelled().size());
            assertEquals(0, entrants.get().accepted().size());
        });
    }
}
