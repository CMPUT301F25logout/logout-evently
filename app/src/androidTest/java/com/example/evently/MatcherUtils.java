package com.example.evently;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

import java.util.ArrayList;

import android.util.Pair;
import android.view.View;
import androidx.annotation.IdRes;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.contrib.RecyclerViewActions;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

public final class MatcherUtils {
    public static <A, B> Pair<A, B> p(A first, B second) {
        return new Pair<>(first, second);
    }

    /**
     * Assert that the given item (identified by a descendant ID and associated text) exists within
     * the recycler view.
     * @param recyclerViewId Target recycler view.
     * @param expectedDescendants One or more pairs of descendant (of recyclerview) with associated text.
     */
    @SafeVarargs
    public static void assertRecyclerViewItem(
            @IdRes int recyclerViewId, Pair<Integer, String>... expectedDescendants) {
        final var matchers = new ArrayList<Matcher<? super View>>();
        for (Pair<Integer, String> pair : expectedDescendants) {
            matchers.add(hasDescendant(allOf(withId(pair.first), withText(pair.second))));
        }
        Matcher<View> allDescendantsMatcher = Matchers.allOf(matchers);

        // Scroll to the item with given descendants.
        onView(allOf(withId(recyclerViewId), isDisplayed()))
                .perform(RecyclerViewActions.scrollTo(allDescendantsMatcher));

        // Make sure all the expected descendants are displayed.
        for (final var expectedDescendant : expectedDescendants) {
            onView(withText(expectedDescendant.second)).check(matches(isDisplayed()));
        }
    }

    /**
     * Select a particular tab in a given tab layout with the tab title.
     * @param tabLayoutId ID of the tab layout we're working with.
     * @param tabTitle Visible title of the tab in said tab layout.
     * @return ViewInteraction to do further actions on.
     */
    public static ViewInteraction onTabWithText(@IdRes int tabLayoutId, String tabTitle) {
        return onView(allOf(withParent(withId(tabLayoutId)), withText(tabTitle)));
    }

    private MatcherUtils() {}
}
