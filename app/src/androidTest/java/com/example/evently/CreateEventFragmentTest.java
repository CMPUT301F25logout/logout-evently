package com.example.evently;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ExecutionException;

import android.widget.TextView;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.AfterClass;
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
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

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
        final LocalDate selectionDate = LocalDate.of(2030, 1, 1);
        final LocalDate eventDate = LocalDate.of(2030, 1, 2);
        final LocalTime eventTime = LocalTime.NOON;

        setEventSchedule(selectionDate, eventDate, eventTime);

        // Fill fields
        onView(withId(R.id.etEventName)).perform(replaceText(name), closeSoftKeyboard());
        onView(withId(R.id.etDescription)).perform(replaceText(desc), closeSoftKeyboard());
        onView(withId(R.id.etWinners)).perform(replaceText(winners), closeSoftKeyboard());

        // Submit
        onView(withId(R.id.btnCreate)).perform(scrollTo(), click());

        // Verify persisted to Firestore
        List<Event> mine = eventsDB.fetchEventsByOrganizers(self).await();
        assertTrue(mine.stream()
                .anyMatch(e -> e.name().equals(name) && e.description().equals(desc)));
    }

    @Test
    public void createEvent_invalidWinnersTest() throws Exception {
        final String self = FirebaseAuthUtils.getCurrentEmail();
        final int before = eventsDB.fetchEventsByOrganizers(self).await().size();

        setEventSchedule(LocalDate.of(2030, 1, 1), LocalDate.of(2030, 1, 2), null);

        onView(withId(R.id.etEventName)).perform(replaceText("Bad Winners"), closeSoftKeyboard());
        onView(withId(R.id.etDescription)).perform(replaceText("desc"), closeSoftKeyboard());
        onView(withId(R.id.etWinners)).perform(replaceText("abc"), closeSoftKeyboard());

        onView(withId(R.id.btnCreate)).perform(scrollTo(), click());

        int after = eventsDB.fetchEventsByOrganizers(self).await().size();
        assertEquals(before, after);
    }

    @Test
    public void createEvent_invalidWaitlistTest() throws Exception {
        final String self = FirebaseAuthUtils.getCurrentEmail();
        final int before = eventsDB.fetchEventsByOrganizers(self).await().size();

        setEventSchedule(LocalDate.of(2030, 1, 1), LocalDate.of(2030, 1, 2), LocalTime.of(9, 0));

        onView(withId(R.id.etEventName)).perform(replaceText("Bad Wait"), closeSoftKeyboard());
        onView(withId(R.id.etDescription)).perform(replaceText("desc"), closeSoftKeyboard());
        onView(withId(R.id.etWinners)).perform(replaceText("5"), closeSoftKeyboard());
        onView(withId(R.id.etWaitLimit)).perform(replaceText("xyz"), closeSoftKeyboard());

        onView(withId(R.id.btnCreate)).perform(scrollTo(), click());

        int after = eventsDB.fetchEventsByOrganizers(self).await().size();
        assertEquals(before, after);
    }

    @Test
    public void createEvent_invalidDateTest() throws Exception {
        final String self = FirebaseAuthUtils.getCurrentEmail();
        final int before = eventsDB.fetchEventsByOrganizers(self).await().size();

        setEventSchedule(LocalDate.of(2030, 1, 2), LocalDate.of(2030, 1, 1), LocalTime.of(12, 0));

        onView(withId(R.id.etEventName)).perform(replaceText("Bad Date"), closeSoftKeyboard());
        onView(withId(R.id.etDescription)).perform(replaceText("desc"), closeSoftKeyboard());
        onView(withId(R.id.etWinners)).perform(replaceText("3"), closeSoftKeyboard());
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

    private void setEventSchedule(
            LocalDate selectionDate, LocalDate eventDate, LocalTime eventTime) {
        scenario.onFragment(fragment -> {
            setPrivateField(fragment, "selectionDeadline", selectionDate);
            setPrivateField(fragment, "eventDate", eventDate);
            setPrivateField(fragment, "eventTime", eventTime);

            TextView selectionInput = fragment.requireView().findViewById(R.id.etSelectionDeadline);
            selectionInput.setText(DATE_FORMATTER.format(selectionDate));
            TextView eventDateInput = fragment.requireView().findViewById(R.id.etEventDate);
            eventDateInput.setText(DATE_FORMATTER.format(eventDate));
            TextView eventTimeInput = fragment.requireView().findViewById(R.id.etEventTime);
            if (eventTime != null) {
                eventTimeInput.setText(TIME_FORMATTER.format(eventTime));
            } else {
                eventTimeInput.setText("");
            }
        });
    }

    private void setPrivateField(CreateEventFragment fragment, String fieldName, Object value) {
        try {
            Field field = CreateEventFragment.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(fragment, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalStateException("Unable to set field " + fieldName, e);
        }
    }
}
