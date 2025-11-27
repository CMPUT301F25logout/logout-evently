package com.example.evently;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import android.os.Bundle;
import androidx.navigation.NavGraph;

import com.google.firebase.Timestamp;
import org.junit.After;
import org.junit.Before;
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
public class NotificationThreadTest extends EmulatedFragmentTest<NotificationThread> {
    private static final EventsDB eventsDB = new EventsDB();
    private static final NotificationDB notificationDB = new NotificationDB();
    private static final Instant now = Instant.now();
    // We can use the same times for these tests.
    private static final Timestamp selectionTime = new Timestamp(now.plus(Duration.ofMillis(100)));
    private static final Timestamp eventTime = new Timestamp(now.plus(Duration.ofMinutes(10)));
    private static final Account mockAccount = defaultMockAccount;

    // Create a event.
    private static final Event mockEvent = new Event(
            "name",
            "description",
            Category.EDUCATIONAL,
            selectionTime,
            eventTime,
            mockAccount.email(),
            50);

    @Override
    protected Bundle getSelfDestinationArgs() {
        Bundle args = new Bundle();
        args.putSerializable("eventID", mockEvent.eventID());
        args.putSerializable("channel", "All");
        return args;
    }

    /**
     * Stores some notifications for the tests. Not functionally necessary, since ViewNotifThreads
     * will be tested in another class.
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Before
    public void setUpNotifications() throws ExecutionException, InterruptedException {

        // Store event into DB.
        Promise.all(eventsDB.storeEvent(mockEvent)).await();

        final var promises = new ArrayList<Promise<Void>>();
        // Sends some event notifications
        promises.add(notificationDB.storeNotification(templateNotification(0, Channel.All)));
        promises.add(notificationDB.storeNotification(templateNotification(1, Channel.All)));
        promises.add(notificationDB.storeNotification(templateNotification(3, Channel.All)));
        promises.add(notificationDB.storeNotification(templateNotification(7, Channel.All)));
        promises.add(notificationDB.storeNotification(templateNotification(8, Channel.All)));
        promises.add(notificationDB.storeNotification(templateNotification(1, Channel.Winners)));
        promises.add(notificationDB.storeNotification(templateNotification(2, Channel.Winners)));
        promises.add(notificationDB.storeNotification(templateNotification(4, Channel.Winners)));
        promises.add(notificationDB.storeNotification(templateNotification(5, Channel.Winners)));
        promises.add(notificationDB.storeNotification(templateNotification(6, Channel.Winners)));

        // Sends the notifications
        Promise.all(promises.stream()).await();
    }

    @After
    public void tearDownNotifications() throws ExecutionException, InterruptedException {
        Promise.all(notificationDB.nuke(), eventsDB.nuke()).await();
    }

    @Test
    public void test_valid_notification() throws InterruptedException, ExecutionException {

        final String title = "Valid Notif!";
        final String description = "New Notif without title!";

        // Fill title & description out
        onView(withId(R.id.etTitle)).perform(replaceText(title), closeSoftKeyboard());
        onView(withId(R.id.etDescription)).perform(replaceText(description), closeSoftKeyboard());

        // Submit it
        onView(withId(R.id.btnSendNotification)).perform(click());
        Thread.sleep(1000);

        // Asserts that the notification has been created, and sent to the correct channel.
        List<Notification> mine = notificationDB
                .fetchNotificationsByOrganizer(mockAccount.email())
                .await();
        assertTrue(mine.stream().anyMatch(e -> {
            boolean hasDescription = e.description().equals(description);
            boolean hasTitle = e.title().equals(title);
            boolean hasCorrectChannel = e.channel().equals(Channel.All);
            return hasDescription && hasTitle && hasCorrectChannel;
        }));
    }

    @Test
    public void test_no_description() throws InterruptedException, ExecutionException {

        final String title = "New Notif without desc!";

        // Fill title
        onView(withId(R.id.etTitle)).perform(replaceText(title), closeSoftKeyboard());

        // Submit
        onView(withId(R.id.btnSendNotification)).perform(click());
        Thread.sleep(1000);

        // Asserts that it is not submitted
        List<Notification> mine = notificationDB
                .fetchNotificationsByOrganizer(mockAccount.email())
                .await();
        assertFalse(mine.stream().anyMatch(e -> e.title().equals(title)));
    }

    @Test
    public void test_no_title() throws InterruptedException, ExecutionException {

        final String description = "New Notif without title!";

        // Fill title
        onView(withId(R.id.etDescription)).perform(replaceText(description), closeSoftKeyboard());

        // Submit
        onView(withId(R.id.btnSendNotification)).perform(click());
        Thread.sleep(1000);

        // Asserts that it is not submitted
        List<Notification> mine = notificationDB
                .fetchNotificationsByOrganizer(mockAccount.email())
                .await();
        assertFalse(mine.stream().anyMatch(e -> e.description().equals(description)));
    }

    /**
     * Helper function to create a notification from the event index and target channel.
     * @param offset A number used for creating notifications from same channel with different
     *            id's
     * @param channel The channel of the communication
     * @return a notification for the selected event.
     */
    private static Notification templateNotification(Integer offset, Notification.Channel channel) {

        /**
         * The following code is for generating a UUID based on the inputs to this function, and
         * is based on the following stack overflow reply:
         * Title: "Is there any way to generate the same UUID from a String"
         * Author: Adam Lee
         * Response Author: "uraimo"
         * Response Link: https://stackoverflow.com/a/29059595
         */
        String notification_input = offset.toString() + channel.toString();
        UUID notificationID = UUID.nameUUIDFromBytes(notification_input.getBytes());

        return new Notification(
                notificationID,
                mockEvent.eventID(),
                channel,
                channel + " channel " + offset,
                "Description (" + channel + ") " + offset);
    }

    @Override
    protected int getGraph() {
        return R.navigation.organizer_graph;
    }

    @Override
    protected int getSelfDestination(NavGraph graph) {
        return R.id.nav_thread;
    }

    @Override
    protected Class<NotificationThread> getFragmentClass() {
        return NotificationThread.class;
    }
}
