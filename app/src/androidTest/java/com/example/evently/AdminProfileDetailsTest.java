package com.example.evently;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.os.Bundle;
import androidx.navigation.NavGraph;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.example.evently.ui.admin.ViewProfileDetailsFragment;

@RunWith(AndroidJUnit4.class)
public class AdminProfileDetailsTest extends EmulatedFragmentTest<ViewProfileDetailsFragment> {

    @Test
    public void testViewingProfileDetails() throws InterruptedException {
        Thread.sleep(2000);

        // Test for name and email being displayed
        onView(withText("Name: " + defaultMockAccount.name())).check(matches(isDisplayed()));
        onView(withText("Email: " + defaultMockAccount.email())).check(matches(isDisplayed()));
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
        bundle.putSerializable("accountEmail", defaultMockAccount.email());
        return bundle;
    }

    @Override
    protected Class<ViewProfileDetailsFragment> getFragmentClass() {
        return ViewProfileDetailsFragment.class;
    }
}
