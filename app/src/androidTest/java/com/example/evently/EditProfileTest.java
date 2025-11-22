package com.example.evently;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import java.util.concurrent.ExecutionException;

import androidx.navigation.NavGraph;

import org.junit.BeforeClass;
import org.junit.Test;

import com.example.evently.data.AccountDB;
import com.example.evently.ui.common.EditProfileFragment;

public class EditProfileTest extends EmulatedFragmentTest<EditProfileFragment> {

    /**
     * Creates and stores the account in the DB before profile page is created
     */
    @BeforeClass
    public static void setupAccount() throws ExecutionException, InterruptedException {
        AccountDB db = new AccountDB();
        db.storeAccount(FirebaseEmulatorTest.defaultMockAccount).await();
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
