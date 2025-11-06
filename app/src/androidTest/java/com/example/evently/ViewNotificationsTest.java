package com.example.evently;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static com.example.evently.MatcherUtils.assertRecyclerViewItem;
import static com.example.evently.MatcherUtils.p;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ExecutionException;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.example.evently.data.EventsDB;
import com.example.evently.data.NotificationDB;
import com.example.evently.data.model.Category;
import com.example.evently.data.model.Event;
import com.example.evently.data.model.Notification;
import com.example.evently.ui.entrant.EntrantActivity;

@RunWith(AndroidJUnit4.class)
public class ViewNotificationsTest extends FirebaseEmulatorTest {
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

    @Rule
    public ActivityScenarioRule<EntrantActivity> scenario =
            new ActivityScenarioRule<>(EntrantActivity.class);

    @Before
    public void switchNavigation() {
        onView(withId(R.id.nav_notifs)).perform(click());
    }

    @BeforeClass
    public static void setUpNotifications() {
        final var self = FirebaseEmulatorTest.mockAccount.email();

        // Store events into DB.
        for (final var mockEvent : mockEvents) {
            eventsDB.storeEvent(mockEvent);
        }

        // Enroll self into some of these (not all!).
        eventsDB.enroll(mockEvents[1].eventID(), self);
        eventsDB.enroll(mockEvents[2].eventID(), self);
        eventsDB.enroll(mockEvents[3].eventID(), self);
        eventsDB.enroll(mockEvents[4].eventID(), self);
        eventsDB.enroll(mockEvents[5].eventID(), self);
        eventsDB.enroll(mockEvents[6].eventID(), self);
        eventsDB.enroll(mockEvents[7].eventID(), self);

        // Send a few notifications to all channel.
        notificationDB.storeNotification(templateNotification(0, Notification.Channel.All));
        notificationDB.storeNotification(templateNotification(1, Notification.Channel.All));
        notificationDB.storeNotification(templateNotification(3, Notification.Channel.All));
        notificationDB.storeNotification(templateNotification(7, Notification.Channel.All));
        notificationDB.storeNotification(templateNotification(8, Notification.Channel.All));

        // Also mark self as winner for some of those.
        eventsDB.addSelected(mockEvents[2].eventID(), self);
        eventsDB.addSelected(mockEvents[3].eventID(), self);
        eventsDB.addSelected(mockEvents[4].eventID(), self);
        eventsDB.addSelected(mockEvents[5].eventID(), self);
        eventsDB.addSelected(mockEvents[6].eventID(), self);
        eventsDB.addSelected(mockEvents[7].eventID(), self);

        // Notifications to the winners channel (for every event).
        notificationDB.storeNotification(templateNotification(1, Notification.Channel.Winners));
        notificationDB.storeNotification(templateNotification(2, Notification.Channel.Winners));
        notificationDB.storeNotification(templateNotification(3, Notification.Channel.Winners));
        notificationDB.storeNotification(templateNotification(4, Notification.Channel.Winners));
        notificationDB.storeNotification(templateNotification(5, Notification.Channel.Winners));
        notificationDB.storeNotification(templateNotification(6, Notification.Channel.Winners));
        notificationDB.storeNotification(templateNotification(7, Notification.Channel.Winners));
        notificationDB.storeNotification(templateNotification(8, Notification.Channel.Winners));

        // Some notifications for the losers channel (for a few events).
        notificationDB.storeNotification(templateNotification(1, Notification.Channel.Losers));
        notificationDB.storeNotification(templateNotification(2, Notification.Channel.Losers));
        notificationDB.storeNotification(templateNotification(3, Notification.Channel.Losers));
        notificationDB.storeNotification(templateNotification(7, Notification.Channel.Losers));

        // And some notifications for the cancelled channel (for a few events).
        notificationDB.storeNotification(templateNotification(1, Notification.Channel.Cancelled));
        notificationDB.storeNotification(templateNotification(2, Notification.Channel.Cancelled));
        notificationDB.storeNotification(templateNotification(3, Notification.Channel.Cancelled));
        notificationDB.storeNotification(templateNotification(5, Notification.Channel.Cancelled));
    }

    @AfterClass
    public static void tearDownNotifications() throws ExecutionException, InterruptedException {
        Tasks.await(Tasks.whenAllSuccess(notificationDB.nuke(), eventsDB.nuke()));
    }

    @Test
    public void expectNotification_all() throws InterruptedException {
        // Any notifications sent to the All channel for participated event IDs should show up.
        // See the setUpNotifications to figure out which notifications we're expecting here.
        final var expectedNotifications = new Notification[] {
            templateNotification(1, Notification.Channel.All),
            templateNotification(3, Notification.Channel.All),
            templateNotification(7, Notification.Channel.All)
        };

        // For each of the expected notifications, scroll to it and make sure it shows properly.
        for (final var expectedNotification : expectedNotifications) {
            assertRecyclerViewItem(
                    R.id.notif_list,
                    p(R.id.notif_title, expectedNotification.title()),
                    p(R.id.notif_description, expectedNotification.description()));
        }
    }

    @Test
    public void expectNotification_winner() throws InterruptedException {
        // Notifications sent to the Winner channel for won event IDs should show up.

    }

    @Test
    public void expectNotification_loser() throws InterruptedException {
        // Any notifications sent to the Loser channel for lost event IDs should show up.

    }

    @Test
    public void acceptInvitation_markSeen() throws InterruptedException {
        // Accepting an invitation should put self in the accepted list of entrants
        // and mark the notification as seen.

    }

    @Test
    public void declineInvitation_markSeen_expectNotification_cancelled()
            throws InterruptedException {
        // Declining an invitation should put self in the cancelled list of entrants
        // and mark the notification as seen. Also expose any cancelled channel notifications.

    }

    @Test
    public void noExpectNotification_otherEvent() throws InterruptedException {
        // Notifications from non-participated event should NOT show up.
    }

    @Test
    public void noExpectNotification_otherChannel() throws InterruptedException {
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
}
