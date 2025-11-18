package com.example.evently.ui.organizer;

import static com.example.evently.ui.common.EntrantsFragment.AcceptedEntrantsFragment;
import static com.example.evently.ui.common.EntrantsFragment.CancelledEntrantsFragment;
import static com.example.evently.ui.common.EntrantsFragment.EnrolledEntrantsFragment;
import static com.example.evently.ui.common.EntrantsFragment.SelectedEntrantsFragment;

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

import com.example.evently.databinding.FragmentEventPeopleBinding;

/**
 * Fragment that displays the tabs for event participants:
 * Enrolled, Cancelled, and Selected.
 * Uses ViewPager2 and TabLayout.
 */
public class EventPeopleFragment extends Fragment {

    private FragmentEventPeopleBinding binding;

    @Nullable @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentEventPeopleBinding.inflate(getLayoutInflater(), container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final TabLayout tabLayout = binding.eventPeopleTabLayout;
        final ViewPager2 viewPager = binding.eventPeopleViewPager;

        // Must use parent fragment manager so that the children tabs will have access to
        // viewmodel...
        viewPager.setAdapter(new EventPeopleAdapter(getParentFragmentManager(), getLifecycle()));

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
                    switch (position) {
                        case 0 -> tab.setText("Enrolled");
                        case 1 -> tab.setText("Selected");
                        case 2 -> tab.setText("Accepted");
                        case 3 -> tab.setText("Cancelled");
                    }
                })
                .attach();
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
            final var frag =
                    switch (position) {
                        case 0 -> new EnrolledEntrantsFragment();
                        case 1 -> new SelectedEntrantsFragment();
                        case 2 -> new AcceptedEntrantsFragment();
                        case 3 -> new CancelledEntrantsFragment();
                        // This should never happen. See getItemCount.
                        default -> new Fragment();
                    };
            return frag;
        }

        @Override
        public int getItemCount() {
            return 4;
        }
    }
}
