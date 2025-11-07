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
import static org.junit.Assert.fail;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
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
import com.example.evently.data.model.EventEntrants;
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
        final var self = FirebaseEmulatorTest.mockAccount.email();

        // Store events into DB.
        Promise.all(Arrays.stream(mockEvents).map(eventsDB::storeEvent)).await();

        // Enroll self into some of these (not all!).
        Promise.all(
                        eventsDB.enroll(mockEvents[1].eventID(), self),
                        eventsDB.enroll(mockEvents[2].eventID(), self),
                        eventsDB.enroll(mockEvents[3].eventID(), self),
                        eventsDB.enroll(mockEvents[4].eventID(), self),
                        eventsDB.enroll(mockEvents[5].eventID(), self),
                        eventsDB.enroll(mockEvents[6].eventID(), self),
                        eventsDB.enroll(mockEvents[7].eventID(), self))
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
                        eventsDB.addSelected(mockEvents[2].eventID(), self),
                        eventsDB.addSelected(mockEvents[3].eventID(), self),
                        eventsDB.addSelected(mockEvents[4].eventID(), self),
                        eventsDB.addSelected(mockEvents[5].eventID(), self),
                        eventsDB.addSelected(mockEvents[6].eventID(), self),
                        eventsDB.addSelected(mockEvents[7].eventID(), self))
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
    public void expectNotification_all() throws InterruptedException {
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
    public void expectNotification_winner() throws InterruptedException {
        // Notifications sent to the Winner channel for won event IDs should show up.
        final var expectedNotifications = new Notification[] {
            templateNotification(2, Notification.Channel.Winners),
            templateNotification(3, Notification.Channel.Winners),
            templateNotification(4, Notification.Channel.Winners),
            templateNotification(5, Notification.Channel.Winners),
            templateNotification(6, Notification.Channel.Winners),
            templateNotification(7, Notification.Channel.Winners),
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
    public void expectNotification_loser() throws InterruptedException {
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
    public void acceptInvitation_markSeen() throws InterruptedException, ExecutionException {
        // Accepting an invitation should put self in the accepted list of entrants
        // and mark the notification as seen.
        Notification invite = templateNotification(2, Notification.Channel.Winners);
        List<UUID> eventIDs = new ArrayList<>();
        eventIDs.add(invite.eventId());

        Thread.sleep(2000);
        //
        // Confirms notification is not yet accepted.
        List<EventEntrants> eventEntrantsList =
                eventsDB.fetchEventEntrants(eventIDs).await();
        if (eventEntrantsList.isEmpty()) fail();
        assertFalse(eventEntrantsList.get(0).accepted().contains(mockAccount.email()));

        Thread.sleep(2000);

        // Clicks on a winning notification
        onView(withId(R.id.notif_list))
                .perform(actionOnItem(hasDescendant(withText(invite.title())), click()));
        Thread.sleep(1000);

        // Accepts the winning notification
        onView(withText("Accept")).perform(click());

        Thread.sleep(2000);

        // Confirms notification is accepted.
        eventEntrantsList = eventsDB.fetchEventEntrants(eventIDs).await();
        if (eventEntrantsList.isEmpty()) fail();
        assertTrue(eventEntrantsList.get(0).accepted().contains(mockAccount.email()));

        // Confirms notification is seen.
        // TODO: Confirm notification is seen
        // TODO: My new fetch notification function by notification ID is not working :(
        //        Optional<Notification> optionalNotification =
        // notificationDB.fetchNotification(invite.id()).await();
        //        Thread.sleep(2000);
        //
        //        if (!optionalNotification.isPresent()) fail();
        //        Notification fetchedNotification = optionalNotification.get();
        //        assert(fetchedNotification.hasSeen(mockAccount.email()));
    }

    @Test
    public void declineInvitation_markSeen_expectNotification_cancelled()
            throws ExecutionException, InterruptedException {
        // Declining an invitation should put self in the cancelled list of entrants
        // and mark the notification as seen. Also expose any cancelled channel notifications.
        Notification invite = templateNotification(3, Notification.Channel.Winners);
        List<UUID> eventIDs = new ArrayList<>();
        eventIDs.add(invite.eventId());

        Thread.sleep(3000);

        // Confirms notification is not yet canceled.
        List<EventEntrants> eventEntrantsList =
                eventsDB.fetchEventEntrants(eventIDs).await();
        if (eventEntrantsList.isEmpty()) fail();
        assertFalse(eventEntrantsList.get(0).accepted().contains(mockAccount.email()));

        Thread.sleep(3000);

        // Clicks on a winning notification
        onView(withId(R.id.notif_list))
                .perform(actionOnItem(hasDescendant(withText(invite.title())), click()));
        Thread.sleep(2000);

        // Declines the winning notification
        onView(withText("Decline")).perform(click());

        Thread.sleep(2000);

        // Confirms notification is declined.
        eventEntrantsList = eventsDB.fetchEventEntrants(eventIDs).await();
        if (eventEntrantsList.isEmpty()) fail();
        assertTrue(eventEntrantsList.get(0).cancelled().contains(mockAccount.email()));

        // Confirms notification is seen.
        // TODO: Confirm notification is seen
        // TODO: My new fetch notification function by notification ID is not working :(
        //        Optional<Notification> optionalNotification =
        // notificationDB.fetchNotification(invite.id()).await();
        //        Thread.sleep(2000);
        //
        //        if (!optionalNotification.isPresent()) fail();
        //        Notification fetchedNotification = optionalNotification.get();
        //        assert(fetchedNotification.hasSeen(mockAccount.email()));
    }

    @Test
    public void noExpectNotification_otherEvent() throws InterruptedException {
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
    public void noExpectNotification_otherChannel() throws InterruptedException {
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

    // Helper function to create a notification from the event index and target channel.
    private static Notification templateNotification(Integer idx, Notification.Channel channel) {
        return new Notification(
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
    protected Class<ViewNotificationsFragment> getFragmentClass() {
        return ViewNotificationsFragment.class;
    }
}
