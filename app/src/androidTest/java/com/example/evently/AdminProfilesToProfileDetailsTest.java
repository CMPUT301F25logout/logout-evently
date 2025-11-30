package com.example.evently;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static com.example.evently.MatcherUtils.assertRecyclerViewItem;
import static com.example.evently.MatcherUtils.p;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import androidx.navigation.NavGraph;
import androidx.navigation.fragment.NavHostFragment;
import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.example.evently.ui.admin.BrowseProfilesFragment;

@RunWith(AndroidJUnit4.class)
public class AdminProfilesToProfileDetailsTest
        extends EmulatedFragmentTest<BrowseProfilesFragment> {

    @Test
    public void testSwitchingToProfileDetails() throws InterruptedException {
        Thread.sleep(2000);

        assertRecyclerViewItem(R.id.profile_list, p(R.id.profile_name, defaultMockAccount.name()));

        // Test if pressing the profile details button navigates to profile_details
        onView(withId(R.id.btnDetails)).perform(ViewActions.click());
        scenario.onFragment(fragment -> {
            final var dest = NavHostFragment.findNavController(fragment).getCurrentDestination();
            assertNotNull(dest);
            assertEquals(dest.getId(), R.id.profile_details);
        });
    }

    @Override
    protected int getGraph() {
        return R.navigation.admin_graph;
    }

    @Override
    protected Class<BrowseProfilesFragment> getFragmentClass() {
        return BrowseProfilesFragment.class;
    }

    @Override
    protected int getSelfDestination(NavGraph graph) {
        return R.id.nav_accounts;
    }
}
