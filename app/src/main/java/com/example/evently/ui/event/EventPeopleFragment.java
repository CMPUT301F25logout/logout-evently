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
 * Fragment that displays the tabs for event participants:
 * Enrolled, Cancelled, and Selected.
 * Uses ViewPager2 and TabLayout.
 */
public class EventPeopleFragment extends Fragment {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    public EventPeopleFragment() {}

    @Nullable @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_people, container, false);

        tabLayout = view.findViewById(R.id.eventPeopleTabLayout);
        viewPager = view.findViewById(R.id.eventPeopleViewPager);

        viewPager.setAdapter(new EventPeopleAdapter(getChildFragmentManager(), getLifecycle()));

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
                    switch (position) {
                        case 0 -> tab.setText("Enrolled");
                        case 1 -> tab.setText("Cancelled");
                        case 2 -> tab.setText("Selected");
                    }
                })
                .attach();

        return view;
    }

    /**
     * Adapter that provides the fragments for each tab.
     */
    private static class EventPeopleAdapter extends FragmentStateAdapter {

        public EventPeopleAdapter(
                @NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
            super(fragmentManager, lifecycle);
        }

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
