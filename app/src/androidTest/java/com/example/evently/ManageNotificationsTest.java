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

import androidx.navigation.NavGraph;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.evently.data.EventsDB;
import com.example.evently.data.NotificationDB;
import com.example.evently.data.generic.Promise;
import com.example.evently.data.model.Account;
import com.example.evently.data.model.Category;
import com.example.evently.data.model.Event;
import com.example.evently.data.model.Notification;
import com.example.evently.ui.entrant.ViewNotificationsFragment;
import com.example.evently.ui.organizer.ManageNotificationsFragment;
import com.google.firebase.Timestamp;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * The order of the tests is important for this class, as pressing on a notification can change
 * the accepted, canceled, or seen status of a notification, so FixMethodOrder is used from the
 * following article on ordering Junit tests:
 * "The Order of Tests in JUnit", by Fatos Morina.
 * https://www.baeldung.com/junit-5-test-order
 */
@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ManageNotificationsTest extends EmulatedFragmentTest<ManageNotificationsFragment> {
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
                selectionTime,
                eventTime,
                mockAccount.email(),
                50),
        new Event(
                "name1",
                "description1",
                Category.EDUCATIONAL,
                selectionTime,
                eventTime,
                "Other User",
                50),
        new Event(
                "name2",
                "description2",
                Category.EDUCATIONAL,
                selectionTime,
                eventTime,
                mockAccount.email(),
                50),
        new Event(
                "name3",
                "description3",
                Category.EDUCATIONAL,
                selectionTime,
                eventTime,
                mockAccount.email(),
                50),
        new Event(
                "name4",
                "description4",
                Category.EDUCATIONAL,
                selectionTime,
                eventTime,
                mockAccount.email(),
                50),
        new Event(
                "name5",
                "description5",
                Category.EDUCATIONAL,
                selectionTime,
                eventTime,
                mockAccount.email(),
                50),
        new Event(
                "name6",
                "description6",
                Category.EDUCATIONAL,
                selectionTime,
                eventTime,
                mockAccount.email(),
                50),
        new Event(
                "name7",
                "description7",
                Category.EDUCATIONAL,
                selectionTime,
                eventTime,
                mockAccount.email(),
                50),
        new Event(
                "name8",
                mockAccount.email(),
                Category.EDUCATIONAL,
                selectionTime,
                eventTime,
                "orgEmail", // Created by other person.
                50)
    };

    @BeforeClass
    public static void setUpNotifications() throws ExecutionException, InterruptedException {
        // TODO (chase): We need batch writes. No reason for there to be so many independent writes.

        // Store events into DB.
        Promise.all(Arrays.stream(mockEvents).map(eventsDB::storeEvent)).await();

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

//    @AfterClass
//    public static void tearDownNotifications() throws ExecutionException, InterruptedException {
//        Promise.all(notificationDB.nuke(), eventsDB.nuke()).await();
//    }

    @Test
    public void test1_expectNotification_winner() throws InterruptedException {
        // Notifications sent to the Winner channel for won event IDs should show up.
        final var expectedNotifications = new Notification[] {
            templateNotification(0, Notification.Channel.All),
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
    public void test2_noUnExpectedNotification() throws InterruptedException {
        // Notifications from other people's events should not pop up.
        Notification notMyEvent1 = templateNotification(8, Notification.Channel.All);
        Notification notMyEvent2 = templateNotification(1, Notification.Channel.All);


        // Checks that no notifications for other organizer's events show up.
        Thread.sleep(2000);
        onView(withText(notMyEvent1.title())).check(doesNotExist());
        onView(withText(notMyEvent2.title())).check(doesNotExist());
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
        return R.navigation.organizer_graph;
    }

    @Override
    protected int getSelfDestination(NavGraph graph) {
        return R.id.nav_notifs;
    }

    @Override
    protected Class<ManageNotificationsFragment> getFragmentClass() {
        return ManageNotificationsFragment.class;
    }
}
