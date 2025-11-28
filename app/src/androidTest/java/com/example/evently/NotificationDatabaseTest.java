package com.example.evently;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import com.google.firebase.Timestamp;
import org.junit.Test;

import com.example.evently.data.EventsDB;
import com.example.evently.data.NotificationDB;
import com.example.evently.data.model.Category;
import com.example.evently.data.model.Event;
import com.example.evently.data.model.Notification;

public class NotificationDatabaseTest extends FirebaseEmulatorTest {
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
    public void testStoreNotification() throws InterruptedException, ExecutionException {
        NotificationDB notificationDB = new NotificationDB();
        EventsDB eventsDB = new EventsDB();

        // Adds an event to the DB.
        Event event = testEvent();
        eventsDB.storeEvent(event).await();

        // Stores a notification in the DB
        Notification n = getTestNotification(event);
        notificationDB.storeNotification(n).await();
    }

    /**
     * Creates an a notification, and stores it in the BD
     */
    @Test
    public void testFetchEventNotifications() throws InterruptedException, ExecutionException {
        NotificationDB notificationDB = new NotificationDB();
        EventsDB eventsDB = new EventsDB();

        // Adds an event to the DB.
        Event event = testEvent();
        eventsDB.storeEvent(event).await();

        // Stores a notification in the DB
        Notification n = getTestNotification(event);
        notificationDB.storeNotification(n).await();

        // Tests fetch event by event ID
        List<Notification> notificationList =
                notificationDB.fetchEventNotifications(event.eventID()).await();
        assertTrue(notificationList.contains(n));

        // Tests fetch notification by eventID
        notificationList =
                notificationDB.fetchEventNotifications(event.eventID()).await();
        assertTrue(notificationList.contains(n));
    }

    /**
     * Test fetching events by a specific channel.
     */
    @Test
    public void testFetchEventChannelNotifs() throws InterruptedException, ExecutionException {
        NotificationDB notificationDB = new NotificationDB();
        EventsDB eventsDB = new EventsDB();

        // Adds an event to the DB.
        Event event = testEvent();
        eventsDB.storeEvent(event).await();

        // Creates a winning notification, and all notifcation for the event.
        Notification allNotification = getTestNotification(event);
        Notification winnerNotification = new Notification(
                event.eventID(),
                Notification.Channel.Winners,
                "Winner! You are selected",
                "You can join, or not");

        // Stores the notifications
        notificationDB
                .storeNotification(allNotification)
                .alongside(notificationDB.storeNotification(winnerNotification))
                .await();

        // Tests testing fetching for all channel.
        List<Notification> notificationList = notificationDB
                .fetchEventNotifications(event.eventID(), Notification.Channel.All)
                .await();
        assertTrue(notificationList.contains(allNotification));
        assertFalse(notificationList.contains(winnerNotification));
        assertEquals(1, notificationList.size());

        // Tests testing fetching for Winners channel.
        notificationList = notificationDB
                .fetchEventNotifications(event.eventID(), Notification.Channel.Winners)
                .await();
        assertFalse(notificationList.contains(allNotification));
        assertTrue(notificationList.contains(winnerNotification));
        assertEquals(1, notificationList.size());
    }
}
