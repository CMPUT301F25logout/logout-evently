package com.example.evently;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.example.evently.MatcherUtils.assertRecyclerViewItem;
import static com.example.evently.MatcherUtils.p;
import static org.hamcrest.Matchers.allOf;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ExecutionException;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import com.google.firebase.Timestamp;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.example.evently.data.EventsDB;
import com.example.evently.data.model.Category;
import com.example.evently.data.model.Event;
import com.example.evently.ui.organizer.OrganizerActivity;
import com.example.evently.utils.FirebaseAuthUtils;

@RunWith(AndroidJUnit4.class)
public class OrganizerActivityTest extends FirebaseEmulatorTest {

    private final EventsDB eventsDB = new EventsDB();

    @After
    public void tearDown() throws ExecutionException, InterruptedException {
        eventsDB.nuke().await();
    }

    private Event makeEvent(String name, String organizer) {
        Instant now = Instant.now();
        return new Event(
                name,
                "desc for " + name,
                Category.SOCIAL,
                new Timestamp(now.plus(Duration.ofHours(1))),
                new Timestamp(now.plus(Duration.ofDays(1))),
                organizer,
                5L);
    }



    @Test
    public void switchRoleButtonTest() throws Exception {
        try (ActivityScenario<OrganizerActivity> scenario =
                ActivityScenario.launch(OrganizerActivity.class)) {
            onView(withId(R.id.btnSwitchRole))
                    .check(matches(allOf(isDisplayed(), withText("Switch to Entrant"))));
        }
    }

    @Test
    public void createButtonTest() throws Exception {

        try (ActivityScenario<OrganizerActivity> scenario =
                ActivityScenario.launch(OrganizerActivity.class)) {
            onView(withId(R.id.btnCreateEvent)).perform(click());
            onView(withId(R.id.tvCreateTitle))
                    .check(matches(allOf(isDisplayed(), withText("Create Event"))));
        }
    }

    @Test
    public void EventsPersistenceTest() throws Exception {
        final String self = FirebaseAuthUtils.getCurrentEmail();

        // Create two events for the current organizer
        Event e1 = makeEvent("Event A", self);
        Event e2 = makeEvent("Event B", self);
        eventsDB.storeEvent(e1).await();
        eventsDB.storeEvent(e2).await();

        try (ActivityScenario<OrganizerActivity> scenario =
                ActivityScenario.launch(OrganizerActivity.class)) {
            // Force a refresh of OwnEventsFragment
            scenario.recreate();

            // Give the UI a moment to render after async callback
            Thread.sleep(2000);

            // Verify via MatcherUtils
            assertRecyclerViewItem(R.id.event_list, p(R.id.content, "Event A"));
            assertRecyclerViewItem(R.id.event_list, p(R.id.content, "Event B"));
        }
    }
}
