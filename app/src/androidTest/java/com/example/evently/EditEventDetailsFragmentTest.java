package com.example.evently;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ExecutionException;

import android.os.Bundle;
import androidx.navigation.NavGraph;

import com.google.firebase.Timestamp;
import org.junit.BeforeClass;
import org.junit.Test;

import com.example.evently.data.EventsDB;
import com.example.evently.data.NotificationDB;
import com.example.evently.data.generic.Promise;
import com.example.evently.data.model.Account;
import com.example.evently.data.model.Category;
import com.example.evently.data.model.Event;
import com.example.evently.ui.organizer.EditEventDetailsFragment;

/**
 * The following class tests the ManageNotificationsFragment, which displays the notifications an
 * organizer has sent. This test module reuses code from the ViewNotificationsTest.
 */
public class EditEventDetailsFragmentTest extends EmulatedFragmentTest<EditEventDetailsFragment> {
    private static final EventsDB eventsDB = new EventsDB();
    private static final NotificationDB notificationDB = new NotificationDB();
    private static final Instant now = Instant.now();
    // We can use the same times for these tests.
    private static final Timestamp selectionTime = new Timestamp(now.plus(Duration.ofMillis(100)));
    private static final Timestamp eventTime = new Timestamp(now.plus(Duration.ofMinutes(10)));
    private static final Account mockAccount = defaultMockAccount;

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
        return args;
    }

    @BeforeClass
    public static void setUpEntrants() throws ExecutionException, InterruptedException {

        // Store events into DB.
        eventsDB.storeEvent(mockEvent).await();

        // Notifications to the winners channel (for every event, except event 0).
        Promise.all(
                        // Enrolls people
                        eventsDB.enroll(mockEvent.eventID(), mockAccount.email()),
                        eventsDB.unsafeEnroll(mockEvent.eventID(), "foo@bar.com"),
                        eventsDB.unsafeEnroll(mockEvent.eventID(), "bar@bar.com"),
                        eventsDB.unsafeEnroll(mockEvent.eventID(), "baz@bar.com"),
                        eventsDB.unsafeEnroll(mockEvent.eventID(), "lorem@bar.com"),
                        eventsDB.unsafeEnroll(mockEvent.eventID(), "ipsum@bar.com"))
                .await();

        Promise.all(
                        // Select 2 people.
                        eventsDB.addSelected(mockEvent.eventID(), "foo@bar.com"),
                        eventsDB.addSelected(mockEvent.eventID(), "bar@bar.com"),
                        // Accepts 1 person
                        eventsDB.addAccepted(mockEvent.eventID(), "baz@bar.com"),
                        // Cancels 1
                        eventsDB.addSelected(mockEvent.eventID(), "lorem@bar.com"))
                .await();
    }

    @Test
    public void test_expectNotification_winner() throws InterruptedException {
        Thread.sleep(100000);

        Thread.sleep(2000);
    }

    @Test
    public void test_noUnExpectedNotification() throws InterruptedException {}

    // Tests the ability to switch to a thread by clicking on a notification
    @Test
    public void test_SwitchingToThread() throws InterruptedException {}

    @Override
    protected int getGraph() {
        return R.navigation.organizer_graph;
    }

    @Override
    protected int getSelfDestination(NavGraph graph) {
        return R.id.nav_notifs;
    }

    @Override
    protected Class<EditEventDetailsFragment> getFragmentClass() {
        return EditEventDetailsFragment.class;
    }
}
