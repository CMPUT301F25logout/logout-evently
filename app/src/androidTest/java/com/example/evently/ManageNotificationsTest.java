package com.example.evently;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItem;
import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.example.evently.MatcherUtils.assertRecyclerViewItem;
import static com.example.evently.MatcherUtils.p;
import static org.hamcrest.Matchers.is;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import androidx.navigation.NavGraph;
import androidx.navigation.fragment.NavHostFragment;

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
import com.example.evently.ui.organizer.ManageNotificationsFragment;

/**
 * The following class tests the ManageNotificationsFragment, which displays the notifications an
 * organizer has sent. This test module reuses code from the ViewNotificationsTest.
 */
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
                Category.Educational,
                false,
                selectionTime,
                eventTime,
                mockAccount.email(),
                50),
        new Event(
                "name1",
                "description1",
                Category.Educational,
                false,
                selectionTime,
                eventTime,
                "Other User",
                50),
        new Event(
                "name2",
                "description2",
                Category.Educational,
                false,
                selectionTime,
                eventTime,
                mockAccount.email(),
                50),
        new Event(
                "name3",
                "description3",
                Category.Educational,
                false,
                selectionTime,
                eventTime,
                mockAccount.email(),
                50),
        new Event(
                "name4",
                "description4",
                Category.Educational,
                false,
                selectionTime,
                eventTime,
                mockAccount.email(),
                50),
        new Event(
                "name5",
                "description5",
                Category.Educational,
                false,
                selectionTime,
                eventTime,
                mockAccount.email(),
                50),
        new Event(
                "name6",
                "description6",
                Category.Educational,
                false,
                selectionTime,
                eventTime,
                mockAccount.email(),
                50),
        new Event(
                "name7",
                "description7",
                Category.Educational,
                false,
                selectionTime,
                eventTime,
                mockAccount.email(),
                50),
        new Event(
                "name8",
                mockAccount.email(),
                Category.Educational,
                false,
                selectionTime,
                eventTime,
                "Other User",
                50)
    };

    @BeforeClass
    public static void setUpNotifications() throws ExecutionException, InterruptedException {
        // TODO (chase): We need batch writes. No reason for there to be so many independent writes.

        // Store events into DB.
        Promise.all(Arrays.stream(mockEvents).map(eventsDB::storeEvent)).await();

        final var promises = new ArrayList<Promise<Void>>();
        // Notifications to the winners channel (for every event, except event 0).
        promises.add(notificationDB.storeNotification(templateNotification(0, Channel.All)));
        promises.add(notificationDB.storeNotification(templateNotification(1, Channel.All)));
        promises.add(notificationDB.storeNotification(templateNotification(3, Channel.All)));
        promises.add(notificationDB.storeNotification(templateNotification(7, Channel.All)));
        promises.add(notificationDB.storeNotification(templateNotification(8, Channel.All)));
        promises.add(notificationDB.storeNotification(templateNotification(1, Channel.Winners)));
        promises.add(notificationDB.storeNotification(templateNotification(2, Channel.Winners)));
        promises.add(notificationDB.storeNotification(templateNotification(3, Channel.Winners)));
        promises.add(notificationDB.storeNotification(templateNotification(4, Channel.Winners)));
        promises.add(notificationDB.storeNotification(templateNotification(5, Channel.Winners)));
        promises.add(notificationDB.storeNotification(templateNotification(6, Channel.Winners)));
        promises.add(notificationDB.storeNotification(templateNotification(7, Channel.Winners)));
        promises.add(notificationDB.storeNotification(templateNotification(8, Channel.Winners)));

        Promise.all(promises.stream()).await();
    }

    @AfterClass
    public static void tearDownNotifications() throws ExecutionException, InterruptedException {
        Promise.all(notificationDB.nuke(), eventsDB.nuke()).await();
    }

    @Test
    public void test_expectNotification_winner() throws InterruptedException {
        // Notifications for the events the user organized should show up.
        final var expectedNotifications = new Notification[] {
            templateNotification(0, Channel.All),
            templateNotification(2, Channel.Winners),
            templateNotification(3, Channel.Winners),
            templateNotification(4, Channel.Winners),
            templateNotification(5, Channel.Winners),
            templateNotification(6, Channel.Winners),
            templateNotification(7, Channel.Winners)
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
        Notification notMyEvent1 = templateNotification(8, Channel.All);
        Notification notMyEvent2 = templateNotification(1, Channel.All);

        // Checks that no notifications for other organizer's events show up.
        Thread.sleep(2000);
        onView(withText(notMyEvent1.title())).check(doesNotExist());
        onView(withText(notMyEvent2.title())).check(doesNotExist());
    }

    // Tests the ability to switch to a thread by clicking on a notification
    @Test
    public void test_SwitchingToThread() throws InterruptedException {
        Thread.sleep(2000);

        Event expectedEvent = mockEvents[0];
        Notification expectedNotif = templateNotification(0, Channel.All);

        // Confirms the notification is in the recycler view.
        assertRecyclerViewItem(
                R.id.notif_list,
                p(R.id.notif_title, expectedNotif.title()),
                p(R.id.notif_description, expectedNotif.description()));

        // Clicks on a notification
        onView(withId(R.id.notif_list))
                .perform(actionOnItem(hasDescendant(withText(expectedNotif.title())), click()));

        // The following section of code confirms we were brought to the correct fragment, and was
        // created by Anthropic, Claude Sonnet 4.5 with the following prompt:
        // "In java for android, how can you verify that once clicking on an item, the fragment
        // changes to an expected fragment", 2025-11-26
        scenario.onFragment(fragment -> {
            final var dest = NavHostFragment.findNavController(fragment)
                    .getCurrentDestination()
                    .getId();
            assertThat(dest, is(R.id.nav_thread));
        });
    }

    /**
     * Helper function to create a notification from the event index and target channel.
     * @param idx The index of the event for the notification
     * @param channel The channel of the communication
     * @return a notification for the selected event.
     */
    private static Notification templateNotification(Integer idx, Channel channel) {

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
