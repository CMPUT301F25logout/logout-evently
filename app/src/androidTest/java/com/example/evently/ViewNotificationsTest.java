package com.example.evently;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ExecutionException;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.Timestamp;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.example.evently.data.EventsDB;
import com.example.evently.data.NotificationDB;
import com.example.evently.data.generic.Promise;
import com.example.evently.data.model.Category;
import com.example.evently.data.model.Event;
import com.example.evently.data.model.Notification;
import com.example.evently.ui.entrant.ViewNotificationsFragment;

@RunWith(AndroidJUnit4.class)
public class ViewNotificationsTest extends EmulatedFragmentTest<ViewNotificationsFragment> {
    private static final EventsDB eventsDB = new EventsDB();
    private static final NotificationDB notificationDB = new NotificationDB();

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
                50),
        new Event(
                "name1",
                "description1",
                Category.EDUCATIONAL,
                selectionTime,
                eventTime,
                "orgEmail",
                50),
        new Event(
                "name2",
                "description2",
                Category.EDUCATIONAL,
                selectionTime,
                eventTime,
                "orgEmail",
                50),
        new Event(
                "name3",
                "description3",
                Category.EDUCATIONAL,
                selectionTime,
                eventTime,
                "orgEmail",
                50),
        new Event(
                "name4",
                "description4",
                Category.EDUCATIONAL,
                selectionTime,
                eventTime,
                "orgEmail",
                50),
        new Event(
                "name5",
                "description5",
                Category.EDUCATIONAL,
                selectionTime,
                eventTime,
                "orgEmail",
                50),
        new Event(
                "name6",
                "description6",
                Category.EDUCATIONAL,
                selectionTime,
                eventTime,
                "orgEmail",
                50),
        new Event(
                "name7",
                "description7",
                Category.EDUCATIONAL,
                selectionTime,
                eventTime,
                "orgEmail",
                50),
        new Event(
                "name8",
                "description8",
                Category.EDUCATIONAL,
                selectionTime,
                eventTime,
                "orgEmail",
                50)
    };

    @BeforeClass
    public static void setUpNotifications() throws ExecutionException, InterruptedException {
        // TODO (chase): We need batch writes. No reason for there to be so many independent writes.
        // TODO (chase): Also, no need for every single one of these to be sequential.
        final var self = FirebaseEmulatorTest.mockAccount.email();

        // Store events into DB.
        for (final var mockEvent : mockEvents) {
            eventsDB.storeEvent(mockEvent).await();
        }

        // Enroll self into some of these (not all!).
        eventsDB.enroll(mockEvents[1].eventID(), self).await();
        eventsDB.enroll(mockEvents[2].eventID(), self).await();
        eventsDB.enroll(mockEvents[3].eventID(), self).await();
        eventsDB.enroll(mockEvents[4].eventID(), self).await();
        eventsDB.enroll(mockEvents[5].eventID(), self).await();
        eventsDB.enroll(mockEvents[6].eventID(), self).await();
        eventsDB.enroll(mockEvents[7].eventID(), self).await();

        // Send a few notifications to all channel.
        notificationDB
                .storeNotification(templateNotification(0, Notification.Channel.All))
                .await();
        notificationDB
                .storeNotification(templateNotification(1, Notification.Channel.All))
                .await();
        notificationDB
                .storeNotification(templateNotification(3, Notification.Channel.All))
                .await();
        notificationDB
                .storeNotification(templateNotification(7, Notification.Channel.All))
                .await();
        notificationDB
                .storeNotification(templateNotification(8, Notification.Channel.All))
                .await();

        // Also mark self as winner for some of those.
        eventsDB.addSelected(mockEvents[2].eventID(), self).await();
        eventsDB.addSelected(mockEvents[3].eventID(), self).await();
        eventsDB.addSelected(mockEvents[4].eventID(), self).await();
        eventsDB.addSelected(mockEvents[5].eventID(), self).await();
        eventsDB.addSelected(mockEvents[6].eventID(), self).await();
        eventsDB.addSelected(mockEvents[7].eventID(), self).await();

        // Notifications to the winners channel (for every event).
        notificationDB
                .storeNotification(templateNotification(1, Notification.Channel.Winners))
                .await();
        notificationDB
                .storeNotification(templateNotification(2, Notification.Channel.Winners))
                .await();
        notificationDB
                .storeNotification(templateNotification(3, Notification.Channel.Winners))
                .await();
        notificationDB
                .storeNotification(templateNotification(4, Notification.Channel.Winners))
                .await();
        notificationDB
                .storeNotification(templateNotification(5, Notification.Channel.Winners))
                .await();
        notificationDB
                .storeNotification(templateNotification(6, Notification.Channel.Winners))
                .await();
        notificationDB
                .storeNotification(templateNotification(7, Notification.Channel.Winners))
                .await();
        notificationDB
                .storeNotification(templateNotification(8, Notification.Channel.Winners))
                .await();

        // Some notifications for the losers channel (for a few events).
        notificationDB
                .storeNotification(templateNotification(1, Notification.Channel.Losers))
                .await();
        notificationDB
                .storeNotification(templateNotification(2, Notification.Channel.Losers))
                .await();
        notificationDB
                .storeNotification(templateNotification(3, Notification.Channel.Losers))
                .await();
        notificationDB
                .storeNotification(templateNotification(7, Notification.Channel.Losers))
                .await();

        // And some notifications for the cancelled channel (for a few events).
        notificationDB
                .storeNotification(templateNotification(1, Notification.Channel.Cancelled))
                .await();
        notificationDB
                .storeNotification(templateNotification(2, Notification.Channel.Cancelled))
                .await();
        notificationDB
                .storeNotification(templateNotification(3, Notification.Channel.Cancelled))
                .await();
        notificationDB
                .storeNotification(templateNotification(5, Notification.Channel.Cancelled))
                .await();
    }

    @AfterClass
    public static void tearDownNotifications() throws ExecutionException, InterruptedException {
        Promise.all(notificationDB.nuke(), eventsDB.nuke()).await();
    }

    @Test
    public void expectNotification_all() {
        // Any notifications sent to the All channel for participated event IDs should show up.
        // See the setUpNotifications to figure out which notifications we're expecting here.
        final var expectedNotifications = new Notification[] {
            templateNotification(1, Notification.Channel.All),
            templateNotification(3, Notification.Channel.All),
            templateNotification(7, Notification.Channel.All)
        };

        // For each of the expected notifications, scroll to it and make sure it shows properly.
        //        for (final var expectedNotification : expectedNotifications) {
        //            assertRecyclerViewItem(
        //                    R.id.notif_list,
        //                    p(R.id.notif_title, expectedNotification.title()),
        //                    p(R.id.notif_description, expectedNotification.description()));
        //        }
        onView(withId(R.id.notif_list)).check(matches(isDisplayed()));
    }

    @Test
    public void expectNotification_winner() {
        // Notifications sent to the Winner channel for won event IDs should show up.

    }

    @Test
    public void expectNotification_loser() {
        // Any notifications sent to the Loser channel for lost event IDs should show up.

    }

    @Test
    public void acceptInvitation_markSeen() {
        // Accepting an invitation should put self in the accepted list of entrants
        // and mark the notification as seen.

    }

    @Test
    public void declineInvitation_markSeen_expectNotification_cancelled() {
        // Declining an invitation should put self in the cancelled list of entrants
        // and mark the notification as seen. Also expose any cancelled channel notifications.

    }

    @Test
    public void noExpectNotification_otherEvent() {
        // Notifications from non-participated event should NOT show up.
    }

    @Test
    public void noExpectNotification_otherChannel() {
        // Notifications from participated event but non-participated channel should NOT show up.

    }

    // Helper function to create a notification from the event index and target channel.
    private static Notification templateNotification(Integer idx, Notification.Channel channel) {
        return new Notification(
                mockEvents[idx].eventID(),
                channel,
                channel + " channel " + idx,
                "Description " + idx);
    }

    @Override
    protected int getGraph() {
        return R.navigation.entrant_graph;
    }

    @Override
    protected Class<ViewNotificationsFragment> getFragmentClass() {
        return ViewNotificationsFragment.class;
    }
}
