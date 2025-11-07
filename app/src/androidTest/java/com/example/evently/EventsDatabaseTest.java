package com.example.evently;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.concurrent.ExecutionException;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.Timestamp;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.example.evently.data.EventsDB;
import com.example.evently.data.model.Category;
import com.example.evently.data.model.Event;

@RunWith(AndroidJUnit4.class)
public class EventsDatabaseTest extends FirebaseEmulatorTest {
    @Rule
    public ActivityScenarioRule<MainActivity> scenario =
            new ActivityScenarioRule<MainActivity>(MainActivity.class);

    /**
     * Creates an event for testing
     * @return created event
     */
    private Event testEvent() {
        return new Event(
                "testEvent",
                "Event created to test.",
                Category.EDUCATIONAL,
                Timestamp.now(),
                new Timestamp(LocalDate.of(2026, 1, 1)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()),
                "testOrganizer@example.com",
                10L,
                55L);
    }

    /**
     * Creates an event with values altered by num
     * @param num an integer value to include in values
     * @return created event
     */
    private Event testEvent(int num) {
        return new Event(
                "testEvent" + num,
                "Event " + num + " created for testing",
                Category.EDUCATIONAL,
                Timestamp.now(),
                new Timestamp(LocalDate.of(2026 + num, 1, 1)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()),
                "testOrganizer" + num + "@example.com",
                10L,
                50L);
    }

    /**
     * Tests the store, and fetch account operations.
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
        Instant timeCheck = LocalDate.of(2027, 2, 1)
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
}
