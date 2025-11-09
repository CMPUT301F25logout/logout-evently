package com.example.evently;

import java.lang.reflect.InvocationTargetException;

import android.os.Bundle;
import androidx.annotation.IdRes;
import androidx.annotation.NavigationRes;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.navigation.NavGraph;
import androidx.navigation.Navigation;
import androidx.navigation.testing.TestNavHostController;
import androidx.test.core.app.ApplicationProvider;

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
    protected FragmentScenario<T> scenario;

    protected abstract @NavigationRes int getGraph();

    /**
     * Implement this if you're testing a fragment that isn't the start destination of the associated graph.
     * @param graph Graph returned by getGraph - passed in to aid in implementing the default impl.
     * @return The destination ID associated with the fragment being tested (corresponding to the graph).
     */
    protected @IdRes int getSelfDestination(NavGraph graph) {
        return graph.getStartDestinationId();
    }

    /**
     * Goes hand in hand with getSelfDestination. In case your destination requires argument(s). Implement this as well.
     * @return Bundle with the arguments to pass in to the fragment.
     */
    protected Bundle getSelfDestinationArgs() {
        return null;
    }

    protected abstract Class<T> getFragmentClass();

    @Before
    public void setUpFragment() {
        // https://developer.android.com/guide/navigation/testing#java
        final var navController =
                new TestNavHostController(ApplicationProvider.getApplicationContext());

        // Create a graphical FragmentScenario for the fragment.
        scenario = FragmentScenario.launchInContainer(getFragmentClass(), getSelfDestinationArgs(), R.style.Theme_Evently);

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
