package com.example.evently;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.util.Log;

import androidx.test.ext.junit.rules.ActivityScenarioRule;

import com.example.evently.data.EventsDB;
import com.example.evently.data.model.Event;
import com.google.firebase.Timestamp;

import org.junit.Rule;
import org.junit.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

public class EventsDatabaseTest {
    @Rule
    public ActivityScenarioRule<MainActivity> scenario =
            new ActivityScenarioRule<MainActivity>(MainActivity.class);

    private Event testEvent() {
        return new Event(
                "testEvent",
                "Event created to test.",
                LocalDate.now(),
                LocalDateTime.of(2026, 1, 1, 1, 0),
                "testOrganizer@example.com",
                Optional.of(55L),
                10L);
    }

    private Event testEvent(int num) {
        return new Event(
                "testEvent" + num,
                "Event " + num + " created for testing",
                LocalDate.now(),
                LocalDateTime.of(2026 + num, 1, 1, 1, 0),
                "testOrganizer" + num + "@example.com",
                Optional.of(55L),
                10L
        );
    }

    /**
     * Tests the store, and fetch account operations.
     */
    @Test
    public void testStoreAndFetchEvent() throws InterruptedException {

        // The idea for using CountDownLatches for synchronization is from the article below:
        // Article: https://stackoverflow.com/questions/15938538/how-can-i-make-a-junit-test-wait
        // Title: "How can I make a JUnit test wait?"
        // Answer: https://stackoverflow.com/a/64645442
        // License: CC BY-SA 4.0
        CountDownLatch addEventLatch = new CountDownLatch(1);

        EventsDB db = new EventsDB();

        Event event = testEvent();
        db.storeEvent(
                event,
                v -> addEventLatch.countDown(),
                e -> {});
        addEventLatch.await(); // Waits until the account is added

        // The following code fetches the added account, and confirms the query returns the result
        CountDownLatch fetchLatch = new CountDownLatch(1);
        db.fetchEvent(
                event.eventID(),
                fetchedEvent -> {
                    fetchLatch.countDown();

                    assertTrue(fetchedEvent.isPresent());
                    assertEquals(fetchedEvent.get(), event);
                },
                e -> {
                    Log.d("FETCH ACCOUNT", "testStoreAccount: Failed to fetch account");
                });

        fetchLatch.await();
        assertTrue(true);
    }

    /**
     * Test for the deleteEvent function
     */
    @Test
    public void testDeleteEvent() throws InterruptedException {
        CountDownLatch addEventLatch = new CountDownLatch(1);

        EventsDB db = new EventsDB();

        Event event = testEvent();
        db.storeEvent(
                event,
                v -> addEventLatch.countDown(),
                e -> {});
        addEventLatch.await();

        CountDownLatch deleteEventLatch = new CountDownLatch(1);
        db.deleteEvent(
                event.eventID(),
                v -> deleteEventLatch.countDown(),
                e -> {});
        deleteEventLatch.await();

        CountDownLatch fetchLatch = new CountDownLatch(1);

        db.fetchEvent(
                event.eventID(),
                optionalEvent -> {
                    assertFalse(optionalEvent.isPresent());
                    fetchLatch.countDown();
                },
                e -> {});
        fetchLatch.await();
    }

    /**
     * Tests fetching events by organizer
     */
    @Test
    public void testFetchEventByOrganizer() throws InterruptedException{
        CountDownLatch addEventLatch = new CountDownLatch(1);

        EventsDB db = new EventsDB();

        Event event1 = testEvent(1);
        Event event2 = testEvent(2);
        db.storeEvent(
                event1,
                v -> addEventLatch.countDown(),
                e -> {});
        addEventLatch.await();

        db.storeEvent(
                event2,
                v -> addEventLatch.countDown(),
                e -> {});
        addEventLatch.await();

        CountDownLatch fetchLatch = new CountDownLatch(1);

        db.fetchEventsByOrganizers(
                event1.organizer(),
                eventCollection -> {
                    assertTrue(eventCollection.contains(event1));
                    assertFalse(eventCollection.contains(event2));
                    fetchLatch.countDown();
                },
                e -> {});
        fetchLatch.await();

        db.fetchEventsByOrganizers(
                "notAnOrganizer",
                eventCollection -> {
                    assertTrue(eventCollection.isEmpty());
                    fetchLatch.countDown();
                },
                e -> {});
        fetchLatch.await();
    }

    /**
     * Tests fetching events by date
     */
    @Test
    public void testFetchEventByDate() throws InterruptedException{
        CountDownLatch addEventLatch = new CountDownLatch(1);
        Instant timeCheck = LocalDate.of(2027, 2, 1)
                .atStartOfDay()
                .atZone(ZoneId.systemDefault())
                .toInstant();

        EventsDB db = new EventsDB();

        Event event1 = testEvent(1);
        Event event2 = testEvent(2);
        db.storeEvent(
                event1,
                v -> addEventLatch.countDown(),
                e -> {});
        addEventLatch.await();

        db.storeEvent(
                event2,
                v -> addEventLatch.countDown(),
                e -> {});
        addEventLatch.await();

        CountDownLatch fetchLatch = new CountDownLatch(1);

        db.fetchEventsByDate(
                new Timestamp(timeCheck),
                eventCollection -> {
                    assertFalse(eventCollection.contains(event1));
                    assertTrue(eventCollection.contains(event2));
                    fetchLatch.countDown();
                },
                e -> {},
                true);
        fetchLatch.await();

        db.fetchEventsByDate(
                new Timestamp(timeCheck),
                eventCollection -> {
                    assertTrue(eventCollection.contains(event1));
                    assertFalse(eventCollection.contains(event2));
                    fetchLatch.countDown();
                },
                e -> {},
                false);
        fetchLatch.await();
    }
}
