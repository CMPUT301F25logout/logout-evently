package com.example.evently;

import java.util.concurrent.ExecutionException;

import androidx.annotation.NavigationRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.navigation.Navigation;
import androidx.navigation.testing.TestNavHostController;
import androidx.test.core.app.ApplicationProvider;

import com.google.firebase.auth.FirebaseAuth;
import org.junit.After;
import org.junit.Before;

/**
 * Testing infrastructure class to aid in testing a fragment in isolation.
 * Comes hooked up with {@link FirebaseEmulatorTest }
 * <p>
 * Extending classes must implement getGraph in order to expose the navigation graph the fragment is a part of.
 * <p>
 * Extending classes must implement getFragmentClass to essentially return T.class.
 * Java isn't good enough to do this automatically.
 * @param <T> The fragment class you'll be testing.
 * @see FirebaseEmulatorTest
 */
public abstract class EmulatedFragmentTest<T extends Fragment> extends FirebaseEmulatorTest {
    FragmentScenario<T> scenario;

    protected abstract @NavigationRes int getGraph();

    protected abstract Class<T> getFragmentClass();

    @Before
    public void setUpFragment() throws ExecutionException, InterruptedException {
        // We're gonna sign in (AuthActivity is skipped) before spawning the fragment.
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            login();
        }

        // https://developer.android.com/guide/navigation/testing#java
        var navController = new TestNavHostController(ApplicationProvider.getApplicationContext());

        // Create a graphical FragmentScenario for the TitleScreen
        scenario =
                FragmentScenario.launchInContainer(getFragmentClass(), null, R.style.Theme_Evently);

        scenario.onFragment(fragment -> {
            navController.setGraph(getGraph());
            Navigation.setViewNavController(fragment.requireView(), navController);
        });
    }

    @After
    public void teardownFragment() {
        if (scenario != null) {
            scenario.close();
        }
    }
}
