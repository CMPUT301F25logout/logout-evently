package com.example.evently;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.concurrent.ExecutionException;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.example.evently.data.EventsDB;
import com.example.evently.data.model.Event;
import com.example.evently.ui.organizer.CreateEventFragment;
import com.example.evently.utils.FirebaseAuthUtils;

/**
 * UI + DB integration tests for CreateEventFragment.
 * Uses emulator; writes are verified via EventsDB.fetchEventsByOrganizers(self).
 */
@RunWith(AndroidJUnit4.class)
public class CreateEventFragmentTest extends EmulatedFragmentTest<CreateEventFragment> {

    private static final EventsDB eventsDB = new EventsDB();



    @AfterClass
    public static void tearDown() throws ExecutionException, InterruptedException {
        eventsDB.nuke().await();
    }


    @Test
    public void createEvent_SuccessfulFirebaseStoreTest() throws Exception {
        final String self = FirebaseAuthUtils.getCurrentEmail();
        final String name = "UI Test Event";
        final String desc = "Created by CreateEventFragmentTest";
        final String winners = "7";
        final String date = "2030-01-01";
        final String time = "12:00:00";

        // Fill fields
        onView(withId(R.id.etEventName)).perform(replaceText(name), closeSoftKeyboard());
        onView(withId(R.id.etDescription)).perform(replaceText(desc), closeSoftKeyboard());
        onView(withId(R.id.etWinners)).perform(replaceText(winners), closeSoftKeyboard());
        onView(withId(R.id.etRegDate)).perform(replaceText(date), closeSoftKeyboard());
        onView(withId(R.id.etRegTime)).perform(replaceText(time), closeSoftKeyboard());

        // Submit
        onView(withId(R.id.btnCreate)).perform(click());

        // Verify persisted to Firestore
        List<Event> mine = eventsDB.fetchEventsByOrganizers(self).await();
        assertTrue(mine.stream()
                .anyMatch(e -> e.name().equals(name) && e.description().equals(desc)));
    }

    @Test
    public void createEvent_invalidWinnersTest() throws Exception {
        final String self = FirebaseAuthUtils.getCurrentEmail();
        final int before = eventsDB.fetchEventsByOrganizers(self).await().size();

        onView(withId(R.id.etEventName)).perform(replaceText("Bad Winners"), closeSoftKeyboard());
        onView(withId(R.id.etDescription)).perform(replaceText("desc"), closeSoftKeyboard());
        onView(withId(R.id.etWinners)).perform(replaceText("abc"), closeSoftKeyboard());
        onView(withId(R.id.etRegDate)).perform(replaceText("2030-01-01"), closeSoftKeyboard());
        onView(withId(R.id.etRegTime)).perform(replaceText("12:00:00"), closeSoftKeyboard());

        onView(withId(R.id.btnCreate)).perform(click());

        int after = eventsDB.fetchEventsByOrganizers(self).await().size();
        assertEquals(before, after);
    }

    @Test
    public void createEvent_invalidWaitlistTest() throws Exception {
        final String self = FirebaseAuthUtils.getCurrentEmail();
        final int before = eventsDB.fetchEventsByOrganizers(self).await().size();

        onView(withId(R.id.etEventName)).perform(replaceText("Bad Wait"), closeSoftKeyboard());
        onView(withId(R.id.etDescription)).perform(replaceText("desc"), closeSoftKeyboard());
        onView(withId(R.id.etWinners)).perform(replaceText("5"), closeSoftKeyboard());
        onView(withId(R.id.etRegDate)).perform(replaceText("2030-01-01"), closeSoftKeyboard());
        onView(withId(R.id.etRegTime)).perform(replaceText("12:00:00"), closeSoftKeyboard());
        onView(withId(R.id.etWaitLimit)).perform(replaceText("xyz"), closeSoftKeyboard());

        onView(withId(R.id.btnCreate)).perform(click());

        int after = eventsDB.fetchEventsByOrganizers(self).await().size();
        assertEquals(before, after);
    }

    @Test
    public void createEvent_invalidDateTest() throws Exception {
        final String self = FirebaseAuthUtils.getCurrentEmail();
        final int before = eventsDB.fetchEventsByOrganizers(self).await().size();

        onView(withId(R.id.etEventName)).perform(replaceText("Bad Date"), closeSoftKeyboard());
        onView(withId(R.id.etDescription)).perform(replaceText("desc"), closeSoftKeyboard());
        onView(withId(R.id.etWinners)).perform(replaceText("3"), closeSoftKeyboard());
        onView(withId(R.id.etRegDate))
                .perform(replaceText("2030-13-40"), closeSoftKeyboard()); // invalid
        onView(withId(R.id.etRegTime))
                .perform(replaceText("99:99:99"), closeSoftKeyboard()); // invalid

        onView(withId(R.id.btnCreate)).perform(click());

        int after = eventsDB.fetchEventsByOrganizers(self).await().size();
        assertEquals(before, after);
    }

    @Override
    protected int getGraph() {
        return R.navigation.organizer_graph;
    }

    @Override
    protected Class<CreateEventFragment> getFragmentClass() {
        return CreateEventFragment.class;
    }
}
