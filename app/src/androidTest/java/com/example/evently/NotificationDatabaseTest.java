package com.example.evently;

import static org.junit.Assert.assertTrue;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import androidx.test.ext.junit.rules.ActivityScenarioRule;

import com.google.firebase.Timestamp;
import org.junit.Rule;
import org.junit.Test;

import com.example.evently.data.EventsDB;
import com.example.evently.data.NotificationDB;
import com.example.evently.data.model.Category;
import com.example.evently.data.model.Event;
import com.example.evently.data.model.Notification;

public class NotificationDatabaseTest extends FirebaseEmulatorTest {

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

    private Notification getTestNotification(Event event) {
        return new Notification(
                UUID.randomUUID(), // Notification ID
                event.eventID(),
                Notification.Channel.All,
                "YOU HAVE WON A FREE CRUISE!!!",
                "Please provide your credit card number, and last 9 digits of your SSN to claim your prize",
                Instant.now(), // Event creation time.
                new HashSet<>()); // Seen By
    }

    /**
     * Creates an a notification, and stores it in the BD
     */
    @Test
    public void testStoreNotification() throws InterruptedException {

        NotificationDB notificationDB = new NotificationDB();
        EventsDB eventsDB = new EventsDB();

        // Adds an event to the DB.
        CountDownLatch addEventLatch = new CountDownLatch(1);
        Event event = testEvent();
        eventsDB.storeEvent(event, v -> addEventLatch.countDown(), e -> {});
        addEventLatch.await();

        // Stores a notification in the DB
        CountDownLatch addNotificationLatch = new CountDownLatch(1);
        Notification n = getTestNotification(event);
        notificationDB.storeNotification(
                n,
                v -> {
                    addNotificationLatch.countDown();
                },
                e -> {});
        addNotificationLatch.await();
        assertTrue(true);
    }
}
