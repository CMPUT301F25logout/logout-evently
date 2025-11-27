package com.example.evently;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.example.evently.MatcherUtils.assertRecyclerViewItem;
import static com.example.evently.MatcherUtils.p;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import android.os.Bundle;
import androidx.navigation.NavGraph;

import com.google.firebase.Timestamp;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.example.evently.data.EventsDB;
import com.example.evently.data.NotificationDB;
import com.example.evently.data.generic.Promise;
import com.example.evently.data.model.Account;
import com.example.evently.data.model.Category;
import com.example.evently.data.model.Event;
import com.example.evently.data.model.Notification;
import com.example.evently.data.model.Notification.Channel;
import com.example.evently.ui.organizer.NotificationThread;

/**
 * The following class tests the ManageNotificationsFragment, which displays the notifications an
 * organizer has sent. This test module reuses code from the ViewNotificationsTest.
 */
public class ViewThreadNotificationsTest
        extends EmulatedFragmentTest<NotificationThread.ViewThreadNotifications> {
    private static final EventsDB eventsDB = new EventsDB();
    private static final NotificationDB notificationDB = new NotificationDB();
    private static final Instant now = Instant.now();
    // We can use the same times for these tests.
    private static final Timestamp selectionTime = new Timestamp(now.plus(Duration.ofMillis(100)));
    private static final Timestamp eventTime = new Timestamp(now.plus(Duration.ofMinutes(10)));
    private static final Account mockAccount = defaultMockAccount;

    // Create a few events.

    // Create a event.
    private static final Event myEvent = new Event(
            "name",
            "description",
            Category.EDUCATIONAL,
            selectionTime,
            eventTime,
            mockAccount.email(),
            50);
    private static final Event notMyEvent = new Event(
            "name",
            "description",
            Category.EDUCATIONAL,
            selectionTime,
            eventTime,
            "some other email",
            50);

    @Override
    protected Bundle getSelfDestinationArgs() {
        Bundle args = new Bundle();
        args.putSerializable("eventID", myEvent.eventID());
        args.putSerializable("channel", "All");
        return args;
    }

    @BeforeClass
    public static void setUpNotifications() throws ExecutionException, InterruptedException {
        // TODO (chase): We need batch writes. No reason for there to be so many independent writes.

        // Store events into DB.
        eventsDB.storeEvent(myEvent).alongside(eventsDB.storeEvent(notMyEvent)).await();

        final var promises = new ArrayList<Promise<Void>>();
        // Notifications to the winners channel (for every event, except event 0).
        promises.add(notificationDB.storeNotification(templateNotification(0, Channel.All, 0)));
        promises.add(notificationDB.storeNotification(templateNotification(0, Channel.All, 1)));
        promises.add(notificationDB.storeNotification(templateNotification(0, Channel.All, 2)));
        promises.add(notificationDB.storeNotification(templateNotification(1, Channel.All, 0)));
        promises.add(notificationDB.storeNotification(templateNotification(1, Channel.All, 1)));
        promises.add(notificationDB.storeNotification(templateNotification(0, Channel.Winners, 0)));
        promises.add(notificationDB.storeNotification(templateNotification(0, Channel.Winners, 1)));
        Promise.all(promises.stream()).await();
    }

    @AfterClass
    public static void tearDownNotifications() throws ExecutionException, InterruptedException {
        Promise.all(notificationDB.nuke(), eventsDB.nuke()).await();
    }

    @Test
    public void test_expectNotifications() throws InterruptedException {
        // Notifications sent to the Winner channel for won event IDs should show up.
        final var expectedNotifications = new Notification[] {
            templateNotification(0, Channel.All, 0),
            templateNotification(0, Channel.All, 1),
            templateNotification(0, Channel.All, 2)
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
    public void test_noUnExpectedNotification() throws InterruptedException {
        // Notifications from other people's events should not pop up.
        Notification notMyEvent1 = templateNotification(1, Channel.All, 0);
        Notification notMyEvent2 = templateNotification(1, Channel.All, 1);
        Notification notMyEvent3 = templateNotification(0, Channel.Winners, 0);
        Notification notMyEvent4 = templateNotification(0, Channel.Winners, 1);

        // Checks that no notifications for other organizer's events show up.
        Thread.sleep(2000);
        onView(withText(notMyEvent1.title())).check(doesNotExist());
        onView(withText(notMyEvent2.title())).check(doesNotExist());
        onView(withText(notMyEvent3.title())).check(doesNotExist());
        onView(withText(notMyEvent4.title())).check(doesNotExist());
    }

    /**
     * Helper function to create a notification from the event index and target channel.
     * @param idx The index of the event for the notification
     * @param channel The channel of the communication
     * @param offset The offset of the notification, to allow for creation of multiple notifs from
     *               the same channel.
     * @return a notification for the selected event.
     */
    private static Notification templateNotification(Integer idx, Channel channel, Integer offset) {

        /**
         * The following code is for generating a UUID based on the inputs to this function, and
         * is based on the following stack overflow reply:
         * Title: "Is there any way to generate the same UUID from a String"
         * Author: Adam Lee
         * Response Author: "uraimo"
         * Response Link: https://stackoverflow.com/a/29059595
         */
        String notification_input = idx.toString() + channel.toString() + offset.toString();
        UUID notificationID = UUID.nameUUIDFromBytes(notification_input.getBytes());
        Event event = idx == 0 ? myEvent : notMyEvent;

        return new Notification(
                notificationID,
                event.eventID(),
                channel,
                channel + " channel " + idx + " offset " + offset,
                "Description (" + channel + ") " + offset);
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
    protected Class<NotificationThread.ViewThreadNotifications> getFragmentClass() {
        return NotificationThread.ViewThreadNotifications.class;
    }
}
