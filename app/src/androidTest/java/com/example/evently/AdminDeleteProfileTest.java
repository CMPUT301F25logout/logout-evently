package com.example.evently;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

import android.os.Bundle;
import androidx.navigation.NavGraph;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.example.evently.data.AccountDB;
import com.example.evently.data.generic.Promise;
import com.example.evently.data.model.Account;
import com.example.evently.ui.admin.ViewProfileDetailsFragment;

@RunWith(AndroidJUnit4.class)
public class AdminDeleteProfileTest extends EmulatedFragmentTest<ViewProfileDetailsFragment> {
    private static final AccountDB accountsDB = new AccountDB();

    // Create a few mock accounts
    private static final Account testAccount =
            new Account("user@gmail.com", "user", Optional.empty(), "user@gmail.com");

    @BeforeClass
    public static void setUpAccount() throws ExecutionException, InterruptedException {
        accountsDB.storeAccount(testAccount).await();
    }

    @Test
    public void testDeletingAccount() throws InterruptedException {
        Thread.sleep(2000);

        onView(withText("Name: " + testAccount.name())).check(matches(isDisplayed()));
        onView(withText("Email: " + testAccount.email())).check(matches(isDisplayed()));

        // Test if deleting the profile will show the toast for account deletion confirmation
        onView(withId(R.id.delete)).perform(ViewActions.click());
        onView(withId(R.id.confirm_button)).perform(click());

        scenario.onFragment(fragment -> {
            var decorView = fragment.getActivity().getWindow().getDecorView();
            Espresso.onView(withText(R.string.Profile_deleted_toast))
                    .inRoot(withDecorView(not(decorView)))
                    .check(matches(isDisplayed()));
        });
    }

    @AfterClass
    public static void tearDownAccount() throws ExecutionException, InterruptedException {
        Promise.all(accountsDB.nuke()).await();
    }

    @Override
    protected int getGraph() {
        return R.navigation.admin_graph;
    }

    @Override
    protected int getSelfDestination(NavGraph graph) {
        return R.id.profile_details;
    }

    @Override
    protected Bundle getSelfDestinationArgs() {
        final var bundle = new Bundle();
        bundle.putSerializable("accountEmail", testAccount.email());
        return bundle;
    }

    @Override
    protected Class<ViewProfileDetailsFragment> getFragmentClass() {
        return ViewProfileDetailsFragment.class;
    }
}
