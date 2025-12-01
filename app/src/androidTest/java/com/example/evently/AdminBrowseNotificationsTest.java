package com.example.evently;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static com.example.evently.MatcherUtils.assertRecyclerViewItem;
import static com.example.evently.MatcherUtils.p;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
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

import com.example.evently.data.AccountDB;
import com.example.evently.data.EventsDB;
import com.example.evently.data.NotificationDB;
import com.example.evently.data.generic.Promise;
import com.example.evently.data.model.Account;
import com.example.evently.data.model.Category;
import com.example.evently.data.model.Event;
import com.example.evently.data.model.Notification;
import com.example.evently.ui.admin.AdminBrowseNotificationsFragment;

@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AdminBrowseNotificationsTest
        extends EmulatedFragmentTest<AdminBrowseNotificationsFragment> {
    private static final EventsDB eventsDB = new EventsDB();
    private static final AccountDB accountsDB = new AccountDB();
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
                Category.Educational,
                false,
                selectionTime,
                eventTime,
                "orgEmail",
                50),
        new Event(
                "name1",
                "description1",
                Category.Educational,
                false,
                selectionTime,
                eventTime,
                "orgEmail",
                50),
        new Event(
                "name2",
                "description2",
                Category.Educational,
                false,
                selectionTime,
                eventTime,
                "orgEmail",
                50),
        new Event(
                "name3",
                "description3",
                Category.Educational,
                false,
                selectionTime,
                eventTime,
                "orgEmail",
                50),
        new Event(
                "name4",
                "description4",
                Category.Educational,
                false,
                selectionTime,
                eventTime,
                "orgEmail",
                50),
        new Event(
                "name5",
                "description5",
                Category.Educational,
                false,
                selectionTime,
                eventTime,
                "orgEmail",
                50),
        new Event(
                "name6",
                "description6",
                Category.Educational,
                false,
                selectionTime,
                eventTime,
                "orgEmail",
                50),
        new Event(
                "name7",
                "description7",
                Category.Educational,
                false,
                selectionTime,
                eventTime,
                "orgEmail",
                50),
        new Event(
                "name8",
                "description8",
                Category.Educational,
                false,
                selectionTime,
                eventTime,
                "orgEmail",
                50)
    };

    // Create a few mock accounts
    private static final Account[] mockAccounts = new Account[] {
        new Account("user@gmail.com", "user", Optional.empty(), "user@gmail.com"),
        new Account("user1@gmail.com", "user1", Optional.empty(), "user1@gmail.com"),
        new Account("user2@gmail.com", "user2", Optional.empty(), "user2@gmail.com")
    };

    @BeforeClass
    public static void setUpNotifications() throws ExecutionException, InterruptedException {
        // Store events into DB.
        Promise.all(Arrays.stream(mockEvents).map(eventsDB::storeEvent)).await();

        // Store accounts into DB
        Promise.all(Arrays.stream(mockAccounts).map(accountsDB::storeAccount)).await();

        // Enroll accounts into some of these (not all!).
        Promise.all(
                        eventsDB.unsafeEnroll(mockEvents[1].eventID(), mockAccounts[0].email()),
                        eventsDB.unsafeEnroll(mockEvents[2].eventID(), mockAccounts[1].email()),
                        eventsDB.unsafeEnroll(mockEvents[3].eventID(), mockAccounts[2].email()),
                        eventsDB.unsafeEnroll(mockEvents[4].eventID(), mockAccounts[0].email()),
                        eventsDB.unsafeEnroll(mockEvents[5].eventID(), mockAccounts[1].email()),
                        eventsDB.unsafeEnroll(mockEvents[6].eventID(), mockAccounts[2].email()))
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
                        eventsDB.addSelected(mockEvents[2].eventID(), mockAccounts[1].email()),
                        eventsDB.addSelected(mockEvents[4].eventID(), mockAccounts[0].email()),
                        eventsDB.addSelected(mockEvents[6].eventID(), mockAccounts[2].email()))
                .await();

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
        Promise.all(notificationDB.nuke(), eventsDB.nuke(), accountsDB.nuke()).await();
    }

    @Test
    public void test1_expectNotification_all() throws InterruptedException {
        // Any notifications sent to the All channel for participated event IDs should show up.
        // See the setUpNotifications to figure out which notifications we're expecting here.
        final var expectedNotifications = new Notification[] {
            templateNotification(0, Notification.Channel.All),
            templateNotification(1, Notification.Channel.All),
            templateNotification(3, Notification.Channel.All),
            templateNotification(7, Notification.Channel.All),
            templateNotification(8, Notification.Channel.All)
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
    public void test2_expectNotification_winner() throws InterruptedException {
        // Notifications sent to the Winner channel for won event IDs should show up.
        final var expectedNotifications = new Notification[] {
            templateNotification(1, Notification.Channel.Winners),
            templateNotification(2, Notification.Channel.Winners),
            templateNotification(3, Notification.Channel.Winners),
            templateNotification(4, Notification.Channel.Winners),
            templateNotification(5, Notification.Channel.Winners),
            templateNotification(6, Notification.Channel.Winners),
            templateNotification(7, Notification.Channel.Winners),
            templateNotification(8, Notification.Channel.Winners)
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
    public void test3_expectNotification_loser() throws InterruptedException {
        // Any notifications sent to the Loser channel should show up.
        final var expectedNotifications = new Notification[] {
            templateNotification(1, Notification.Channel.Losers),
            templateNotification(2, Notification.Channel.Losers),
            templateNotification(3, Notification.Channel.Losers),
            templateNotification(7, Notification.Channel.Losers)
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
    public void test4_expectNotification_cancelled() throws InterruptedException {
        // Any notifications sent to cancelled should show up
        final var expectedNotifications = new Notification[] {
            templateNotification(1, Notification.Channel.Cancelled),
            templateNotification(2, Notification.Channel.Cancelled),
            templateNotification(3, Notification.Channel.Cancelled),
            templateNotification(5, Notification.Channel.Cancelled)
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
        return R.navigation.admin_graph;
    }

    @Override
    protected int getSelfDestination(NavGraph graph) {
        return R.id.nav_notifs;
    }

    @Override
    protected Class<AdminBrowseNotificationsFragment> getFragmentClass() {
        return AdminBrowseNotificationsFragment.class;
    }
}
