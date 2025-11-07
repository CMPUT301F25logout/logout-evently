package com.example.evently.ui.event;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import com.example.evently.R;

/**
 * Fragment that displays the tabs for event participants in different categories.
 * Uses ViewPager2 and TabLayout to display Enrolled, Cancelled, and Selected entrants.
 */
public class EventPeopleFragment extends Fragment {
    private TabLayout tablayout;
    private ViewPager2 viewPager;

    public EventPeopleFragment() {}

    /**
     * Inflates the layout that this fragment will use
     *
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return
     */
    @Nullable @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_people, container, false);
        tablayout = view.findViewById(R.id.eventPeopleTabLayout);
        viewPager = view.findViewById(R.id.eventPeopleViewPager);

        viewPager.setAdapter(new EventPeopleAdapter(getChildFragmentManager(), getLifecycle()));

        new TabLayoutMediator(tablayout, viewPager, (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText("Enrolled");
                            break;
                        case 1:
                            tab.setText("Cancelled");
                            break;
                        case 2:
                            tab.setText("Selected");
                            break;
                    }
                })
                .attach();

        return view;
    }

    /**
     * Adapter that provides the fragments for each tab (Enrolled, Selected, Cancalled)
     */
    private static class EventPeopleAdapter extends FragmentStateAdapter {
        public EventPeopleAdapter(
                @NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
            super(fragmentManager, lifecycle);
        }

        /**
         * Creates and returns a fragment for the selected tabs
         * @param position Position of the tab
         * @return The corresponding fragment
         */
        @NonNull @Override
        public Fragment createFragment(int position) {
            return switch (position) {
                case 0 -> new AllEntrantFragment.EnrolledPeopleFragment();
                case 1 -> new AllEntrantFragment.CancelledPeopleFragment();
                case 2 -> new AllEntrantFragment.SelectedPeopleFragment();
                default -> new Fragment();
            };
        }

        @Override
        public int getItemCount() {
            return 3;
        }
    }
}
