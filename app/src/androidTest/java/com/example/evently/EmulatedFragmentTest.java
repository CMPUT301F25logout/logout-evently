package com.example.evently;

import java.lang.reflect.InvocationTargetException;

import androidx.annotation.NavigationRes;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.testing.FragmentScenario;
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
    FragmentScenario<T> scenario;

    protected abstract @NavigationRes int getGraph();

    protected abstract Class<T> getFragmentClass();

    @Before
    public void setUpFragment() {
        // See: https://developer.android.com/guide/navigation/testings
        TestNavHostController navController =
                new TestNavHostController(ApplicationProvider.getApplicationContext());

        final var clazz = getFragmentClass();
        scenario = FragmentScenario.launchInContainer(clazz, null, new FragmentFactory() {
            @NonNull @Override
            public Fragment instantiate(
                    @NonNull ClassLoader classLoader, @NonNull String className) {
                T frag;
                try {
                    frag = clazz.getDeclaredConstructor().newInstance();
                } catch (IllegalAccessException
                        | InstantiationException
                        | InvocationTargetException
                        | NoSuchMethodException e) {
                    throw new IllegalArgumentException(
                            "getFragmentClass returned invalid fragment class", e);
                }

                final T finalFrag = frag;
                frag.getViewLifecycleOwnerLiveData().observeForever(viewLifecycleOwner -> {
                    // The fragment’s view has just been created
                    if (viewLifecycleOwner != null) {
                        navController.setGraph(getGraph());
                        Navigation.setViewNavController(frag.requireView(), navController);
                    }
                });
                return frag;
            }
        });
    }

    @After
    public void teardownFragment() {
        if (scenario != null) {
            scenario.close();
        }
    }
}
