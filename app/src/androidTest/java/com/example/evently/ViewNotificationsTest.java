package com.example.evently;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItem;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.example.evently.MatcherUtils.assertRecyclerViewItem;
import static com.example.evently.MatcherUtils.p;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import androidx.navigation.NavGraph;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.Timestamp;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import com.example.evently.data.EventsDB;
import com.example.evently.data.NotificationDB;
import com.example.evently.data.generic.Promise;
import com.example.evently.data.model.Account;
import com.example.evently.data.model.Category;
import com.example.evently.data.model.Event;
import com.example.evently.data.model.Notification;
import com.example.evently.ui.entrant.ViewNotificationsFragment;

/**
 * The order of the tests is important for this class, as pressing on a notification can change
 * the accepted, canceled, or seen status of a notification, so FixMethodOrder is used from the
 * following article on ordering Junit tests:
 * "The Order of Tests in JUnit", by Fatos Morina.
 * https://www.baeldung.com/junit-5-test-order
 */
@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ViewNotificationsTest extends EmulatedFragmentTest<ViewNotificationsFragment> {
    private static final EventsDB eventsDB = new EventsDB();
    private static final NotificationDB notificationDB = new NotificationDB();
    private static final Instant now = Instant.now();
    // We can use the same times for these tests.
    private static final Timestamp selectionTime = new Timestamp(now.plus(Duration.ofMillis(100)));
    private static final Timestamp eventTime = new Timestamp(now.plus(Duration.ofMinutes(10)));
    private static final Account mockAccount = defaultMockAccount;

    // Create a few events.
    private static final Event[] mockEvents = new Event[] {
        new Event(
                "name",
                "description",
                Category.EDUCATIONAL,
                false,
                selectionTime,
                eventTime,
                "orgEmail",
                50),
        new Event(
                "name1",
                "description1",
                Category.EDUCATIONAL,
                false,
                selectionTime,
                eventTime,
                "orgEmail",
                50),
        new Event(
                "name2",
                "description2",
                Category.EDUCATIONAL,
                false,
                selectionTime,
                eventTime,
                "orgEmail",
                50),
        new Event(
                "name3",
                "description3",
                Category.EDUCATIONAL,
                false,
                selectionTime,
                eventTime,
                "orgEmail",
                50),
        new Event(
                "name4",
                "description4",
                Category.EDUCATIONAL,
                false,
                selectionTime,
                eventTime,
                "orgEmail",
                50),
        new Event(
                "name5",
                "description5",
                Category.EDUCATIONAL,
                false,
                selectionTime,
                eventTime,
                "orgEmail",
                50),
        new Event(
                "name6",
                "description6",
                Category.EDUCATIONAL,
                false,
                selectionTime,
                eventTime,
                "orgEmail",
                50),
        new Event(
                "name7",
                "description7",
                Category.EDUCATIONAL,
                false,
                selectionTime,
                eventTime,
                "orgEmail",
                50),
        new Event(
                "name8",
                "description8",
                Category.EDUCATIONAL,
                false,
                selectionTime,
                eventTime,
                "orgEmail",
                50)
    };

    @BeforeClass
    public static void setUpNotifications() throws ExecutionException, InterruptedException {
        // TODO (chase): We need batch writes. No reason for there to be so many independent writes.

        // Store events into DB.
        Promise.all(Arrays.stream(mockEvents).map(eventsDB::storeEvent)).await();

        // Enroll self into some of these (not all!).
        Promise.all(
                        eventsDB.unsafeEnroll(mockEvents[1].eventID(), mockAccount.email()),
                        eventsDB.unsafeEnroll(mockEvents[2].eventID(), mockAccount.email()),
                        eventsDB.unsafeEnroll(mockEvents[3].eventID(), mockAccount.email()),
                        eventsDB.unsafeEnroll(mockEvents[4].eventID(), mockAccount.email()),
                        eventsDB.unsafeEnroll(mockEvents[5].eventID(), mockAccount.email()),
                        eventsDB.unsafeEnroll(mockEvents[6].eventID(), mockAccount.email()),
                        eventsDB.unsafeEnroll(mockEvents[7].eventID(), mockAccount.email()))
                .await();

        // Send a few notifications to all channel.
        Promise.all(
                        notificationDB.storeNotification(
                                templateNotification(0, Notification.Channel.All)),
                        notificationDB.storeNotification(
                                templateNotification(1, Notification.Channel.All)),
                        notificationDB.storeNotification(
                                templateNotification(3, Notification.Channel.All)),
                        notificationDB.storeNotification(
                                templateNotification(7, Notification.Channel.All)),
                        notificationDB.storeNotification(
                                templateNotification(8, Notification.Channel.All)))
                .await();

        // Also mark self as winner for some of those.
        Promise.all(
                        eventsDB.addSelected(mockEvents[2].eventID(), mockAccount.email()),
                        eventsDB.addSelected(mockEvents[3].eventID(), mockAccount.email()),
                        eventsDB.addSelected(mockEvents[4].eventID(), mockAccount.email()),
                        eventsDB.addSelected(mockEvents[5].eventID(), mockAccount.email()),
                        eventsDB.addSelected(mockEvents[6].eventID(), mockAccount.email()),
                        eventsDB.addSelected(mockEvents[7].eventID(), mockAccount.email()))
                .await();

        // Since there are enrolled people who have been selected, those who are not winners of an
        // event are losers (mockEvents[1], and mockEvents[8])

        final var promises = new ArrayList<Promise<Void>>();
        // Notifications to the winners channel (for every event).
        promises.add(notificationDB.storeNotification(
                templateNotification(1, Notification.Channel.Winners)));
        promises.add(notificationDB.storeNotification(
                templateNotification(2, Notification.Channel.Winners)));
        promises.add(notificationDB.storeNotification(
                templateNotification(3, Notification.Channel.Winners)));
        promises.add(notificationDB.storeNotification(
                templateNotification(4, Notification.Channel.Winners)));
        promises.add(notificationDB.storeNotification(
                templateNotification(5, Notification.Channel.Winners)));
        promises.add(notificationDB.storeNotification(
                templateNotification(6, Notification.Channel.Winners)));
        promises.add(notificationDB.storeNotification(
                templateNotification(7, Notification.Channel.Winners)));
        promises.add(notificationDB.storeNotification(
                templateNotification(8, Notification.Channel.Winners)));

        // Some notifications for the losers channel (for a few events).
        promises.add(notificationDB.storeNotification(
                templateNotification(1, Notification.Channel.Losers)));
        promises.add(notificationDB.storeNotification(
                templateNotification(2, Notification.Channel.Losers)));
        promises.add(notificationDB.storeNotification(
                templateNotification(3, Notification.Channel.Losers)));
        promises.add(notificationDB.storeNotification(
                templateNotification(7, Notification.Channel.Losers)));

        // And some notifications for the cancelled channel (for a few events).
        promises.add(notificationDB.storeNotification(
                templateNotification(1, Notification.Channel.Cancelled)));
        promises.add(notificationDB.storeNotification(
                templateNotification(2, Notification.Channel.Cancelled)));
        promises.add(notificationDB.storeNotification(
                templateNotification(3, Notification.Channel.Cancelled)));
        promises.add(notificationDB.storeNotification(
                templateNotification(5, Notification.Channel.Cancelled)));

        Promise.all(promises.stream()).await();
    }

    @AfterClass
    public static void tearDownNotifications() throws ExecutionException, InterruptedException {
        Promise.all(notificationDB.nuke(), eventsDB.nuke()).await();
    }

    @Test
    public void test1_expectNotification_all() throws InterruptedException {
        // Any notifications sent to the All channel for participated event IDs should show up.
        // See the setUpNotifications to figure out which notifications we're expecting here.
        final var expectedNotifications = new Notification[] {
            templateNotification(1, Notification.Channel.All),
            templateNotification(3, Notification.Channel.All),
            templateNotification(7, Notification.Channel.All)
        };

        // TODO (chase): This is a TERRIBLE way to wait for data to show up.
        Thread.sleep(2000);

        // For each of the expected notifications, scroll to it and make sure it shows properly.
        for (final var expectedNotification : expectedNotifications) {
            assertRecyclerViewItem(
                    R.id.notif_list,
                    p(R.id.notif_title, expectedNotification.title()),
                    p(R.id.notif_description, expectedNotification.description()));
        }
        onView(withId(R.id.notif_list)).check(matches(isDisplayed()));
    }

    @Test
    public void test2_expectNotification_winner() throws InterruptedException {
        // Notifications sent to the Winner channel for won event IDs should show up.
        final var expectedNotifications = new Notification[] {
            templateNotification(2, Notification.Channel.Winners),
            templateNotification(3, Notification.Channel.Winners),
            templateNotification(4, Notification.Channel.Winners),
            templateNotification(5, Notification.Channel.Winners),
            templateNotification(6, Notification.Channel.Winners),
            templateNotification(7, Notification.Channel.Winners)
        };

        Thread.sleep(2000);

        // For each of the expected notifications, scroll to it and make sure it shows properly.
        for (final var expectedNotification : expectedNotifications) {
            assertRecyclerViewItem(
                    R.id.notif_list,
                    p(R.id.notif_title, expectedNotification.title()),
                    p(R.id.notif_description, expectedNotification.description()));
        }

        onView(withId(R.id.notif_list)).check(matches(isDisplayed()));
    }

    @Test
    public void test3_noExpectNotification_otherEvent() throws InterruptedException {
        // Notifications from non-participated event should NOT show up.
        Notification nonParticipatedEvent1 = templateNotification(8, Notification.Channel.Winners);
        Notification nonParticipatedEvent2 = templateNotification(8, Notification.Channel.All);
        Notification nonParticipatedEvent3 = templateNotification(0, Notification.Channel.All);

        // Checks that no notifications for non-enrolled events show up.
        Thread.sleep(2000);
        onView(withText(nonParticipatedEvent1.title())).check(doesNotExist());
        onView(withText(nonParticipatedEvent2.title())).check(doesNotExist());
        onView(withText(nonParticipatedEvent3.title())).check(doesNotExist());
    }

    @Test
    public void test4_noExpectNotification_otherChannel() throws InterruptedException {
        // checks that no notifications are shown when the event is participated in, but enterant is
        // in a different channel:
        Notification notification1 = templateNotification(2, Notification.Channel.Losers);
        Notification notification2 = templateNotification(3, Notification.Channel.Losers);
        Notification notification3 = templateNotification(7, Notification.Channel.Losers);
        Notification notification4 = templateNotification(1, Notification.Channel.Winners);

        // Confirms that none of the notifications whee found.
        Thread.sleep(2000);
        onView(withText(notification1.title())).check(doesNotExist());
        onView(withText(notification2.title())).check(doesNotExist());
        onView(withText(notification3.title())).check(doesNotExist());
        onView(withText(notification4.title())).check(doesNotExist());
    }

    @Test
    public void test5_expectNotification_loser() throws InterruptedException {
        // Any notifications sent to the Loser channel for lost event IDs should show up.
        // Notifications sent to the Winner channel for won event IDs should show up.
        final var expectedNotifications =
                new Notification[] {templateNotification(1, Notification.Channel.Losers)};

        Thread.sleep(2000);

        // For each of the expected notifications, scroll to it and make sure it shows properly.
        for (final var expectedNotification : expectedNotifications) {
            assertRecyclerViewItem(
                    R.id.notif_list,
                    p(R.id.notif_title, expectedNotification.title()),
                    p(R.id.notif_description, expectedNotification.description()));
        }

        onView(withId(R.id.notif_list)).check(matches(isDisplayed()));
    }

    @Test
    public void test6_acceptInvitation_markSeen() throws InterruptedException, ExecutionException {
        // Gets a winning notification, from an enrolled event.
        Notification invite = templateNotification(2, Notification.Channel.Winners);

        // Asserts that we haven't seen the notification
        assertFalse(notificationDB
                .fetchNotification(invite.id())
                .await()
                .get()
                .hasSeen(mockAccount.email()));

        // Confirms notification are not yet accepted.
        assertFalse(eventsDB.fetchEventEntrants(invite.eventId())
                .await()
                .orElseThrow()
                .accepted()
                .contains(mockAccount.email()));
        Thread.sleep(1000);

        // Clicks on a winning notification, and accepts it.
        onView(withId(R.id.notif_list))
                .perform(actionOnItem(hasDescendant(withText(invite.title())), click()));
        onView(withText("Accept")).perform(click());

        // Confirms notification is accepted.
        assertTrue(eventsDB.fetchEventEntrants(invite.eventId())
                .await()
                .orElseThrow()
                .accepted()
                .contains(mockAccount.email()));

        // Confirms notification is seen
        assertTrue(notificationDB
                .fetchNotification(invite.id())
                .await()
                .get()
                .hasSeen(mockAccount.email()));
    }

    @Test
    public void test7_declineInvitation_markSeen_expectNotification_cancelled()
            throws ExecutionException, InterruptedException {
        // Gets a winning notification, from an enrolled event.
        Notification invite = templateNotification(3, Notification.Channel.Winners);

        // Asserts that we haven't seen the new notification
        assertFalse(notificationDB
                .fetchNotification(invite.id())
                .await()
                .get()
                .hasSeen(mockAccount.email()));

        // Confirms notification is not yet canceled.
        assertFalse(eventsDB.fetchEventEntrants(invite.eventId())
                .await()
                .orElseThrow()
                .cancelled()
                .contains(mockAccount.email()));
        Thread.sleep(1000);

        // Clicks on a winning notification, and declines it.
        onView(withId(R.id.notif_list))
                .perform(actionOnItem(hasDescendant(withText(invite.title())), click()));
        onView(withText("Decline")).perform(click());

        // Confirms notification is canceled.
        assertTrue(eventsDB.fetchEventEntrants(invite.eventId())
                .await()
                .orElseThrow()
                .cancelled()
                .contains(mockAccount.email()));

        // Confirms notification is seen.
        assertTrue(notificationDB
                .fetchNotification(invite.id())
                .await()
                .get()
                .hasSeen(mockAccount.email()));
    }

    /**
     * Helper function to create a notification from the event index and target channel.
     * @param idx The index of the event for the notification
     * @param channel The channel of the communication
     * @return a notification for the selected event.
     */
    private static Notification templateNotification(Integer idx, Notification.Channel channel) {

        /**
         * The following code is for generating a UUID based on the inputs to this function, and
         * is based on the following stack overflow reply:
         * Title: "Is there any way to generate the same UUID from a String"
         * Author: Adam Lee
         * Response Author: "uraimo"
         * Response Link: https://stackoverflow.com/a/29059595
         */
        String notification_input = idx.toString() + channel.toString();
        UUID notificationID = UUID.nameUUIDFromBytes(notification_input.getBytes());

        return new Notification(
                notificationID,
                mockEvents[idx].eventID(),
                channel,
                channel + " channel " + idx,
                "Description (" + channel + ") " + idx);
    }

    @Override
    protected int getGraph() {
        return R.navigation.entrant_graph;
    }

    @Override
    protected int getSelfDestination(NavGraph graph) {
        return R.id.nav_notifs;
    }

    @Override
    protected Class<ViewNotificationsFragment> getFragmentClass() {
        return ViewNotificationsFragment.class;
    }
}
