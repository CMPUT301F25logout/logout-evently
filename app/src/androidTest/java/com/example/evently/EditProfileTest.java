package com.example.evently;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertNull;

import java.util.concurrent.ExecutionException;

import androidx.navigation.NavGraph;

import com.google.firebase.auth.FirebaseAuth;

import org.junit.Before;
import org.junit.Test;

import com.example.evently.ui.common.EditProfileFragment;

public class EditProfileTest extends EmulatedFragmentTest<EditProfileFragment> {

    @Before
    public void waitForLoad() throws InterruptedException {
        Thread.sleep(1000);
    }

    /**
     * Tests edit name
     */
    @Test
    public void editNameTest() {
        String testName = "testName";
        onView(withId(R.id.name_text))
                .check(matches(withText(FirebaseEmulatorTest.defaultMockAccount.name())));
        onView(withId(R.id.name_button)).perform(click());
        onView(withId(R.id.text_field)).check(matches(isDisplayed()));
        onView(withId(R.id.text_field)).perform(typeText(testName));
        onView(withId(R.id.confirm_button)).perform(click());
        onView(withId(R.id.name_text)).check(matches(withText(testName)));
    }

    /**
     * Tests edit email
     */
    @Test
    public void editEmailTest() {
        String testEmail = "test@email.com";
        onView(withId(R.id.email_text))
                .check(matches(withText(FirebaseEmulatorTest.defaultMockAccount.visibleEmail())));
        onView(withId(R.id.email_button)).perform(click());
        onView(withId(R.id.text_field)).check(matches(isDisplayed()));
        onView(withId(R.id.text_field)).perform(typeText(testEmail));
        onView(withId(R.id.confirm_button)).perform(click());
        onView(withId(R.id.email_text)).check(matches(withText(testEmail)));
    }

    /**
     * Tests edit phone
     */
    @Test
    public void editPhoneTest() {
        String testPhone1 = "(111) 222-3333";
        String testPhone2 = "4445556666";
        onView(withId(R.id.phone_text)).check(matches(withText("(780) 123-4579")));
        onView(withId(R.id.phone_button)).perform(click());
        onView(withId(R.id.text_field)).check(matches(isDisplayed()));
        onView(withId(R.id.text_field)).perform(typeText(testPhone1));
        onView(withId(R.id.confirm_button)).perform(click());
        onView(withId(R.id.phone_text)).check(matches(withText(testPhone1)));
        onView(withId(R.id.phone_button)).perform(click());
        onView(withId(R.id.text_field)).perform(typeText(testPhone2));
        onView(withId(R.id.confirm_button)).perform(click());
        onView(withId(R.id.phone_text)).check(matches(withText("(444) 555-6666")));
    }

    /**
     * Tests sign out button
     */
    @Test
    public void signOutTest() throws ExecutionException, InterruptedException {
        onView(withId(R.id.sign_out)).perform(click());
        onView(withId(R.id.confirm_button)).perform(click());
        Thread.sleep(1000);
        assertNull(FirebaseAuth.getInstance().getCurrentUser());
        signBackIn();
    }

    /**
     * Gets navigation graph the fragment is present on
     * @return nav graph
     */
    @Override
    protected int getGraph() {
        return R.navigation.entrant_graph;
    }

    /**
     * gets id of self from nav graph
     * @param graph Graph returned by getGraph - passed in to aid in implementing the default impl.
     * @return Id of self
     */
    @Override
    protected int getSelfDestination(NavGraph graph) {
        return R.id.nav_account;
    }

    /**
     * Returns class of fragment tested
     * @return class of EditProfileFragment
     */
    @Override
    protected Class<EditProfileFragment> getFragmentClass() {
        return EditProfileFragment.class;
    }
}
