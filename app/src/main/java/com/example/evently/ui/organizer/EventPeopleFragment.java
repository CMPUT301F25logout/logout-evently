package com.example.evently.ui.organizer;

import static com.example.evently.ui.common.EntrantsFragment.AcceptedEntrantsFragment;
import static com.example.evently.ui.common.EntrantsFragment.CancelledEntrantsFragment;
import static com.example.evently.ui.common.EntrantsFragment.EnrolledEntrantsFragment;
import static com.example.evently.ui.common.EntrantsFragment.SelectedEntrantsFragment;

import java.util.UUID;

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

    @Nullable @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        final var binding =
                FragmentEventPeopleBinding.inflate(getLayoutInflater(), container, false);

        final var args = getArguments();
        assert args != null;
        final var eventID = (UUID) args.getSerializable("eventID");
        assert eventID != null;

        final TabLayout tabLayout = binding.eventPeopleTabLayout;
        final ViewPager2 viewPager = binding.eventPeopleViewPager;

        viewPager.setAdapter(
                new EventPeopleAdapter(getChildFragmentManager(), getLifecycle(), eventID));

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
                    switch (position) {
                        case 0 -> tab.setText("Enrolled");
                        case 1 -> tab.setText("Selected");
                        case 2 -> tab.setText("Accepted");
                        case 3 -> tab.setText("Cancelled");
                    }
                })
                .attach();

        return binding.getRoot();
    }

    /**
     * Adapter that provides the fragments for each tab.
     */
    private static class EventPeopleAdapter extends FragmentStateAdapter {
        private final UUID eventID;

        public EventPeopleAdapter(
                @NonNull FragmentManager fragmentManager,
                @NonNull Lifecycle lifecycle,
                @NonNull UUID eventID) {
            super(fragmentManager, lifecycle);

            this.eventID = eventID;
        }

        @NonNull @Override
        public Fragment createFragment(int position) {
            final var bundle = new Bundle();
            bundle.putSerializable("eventID", eventID);
            final var frag =
                    switch (position) {
                        case 0 -> new EnrolledEntrantsFragment();
                        case 1 -> new SelectedEntrantsFragment();
                        case 2 -> new AcceptedEntrantsFragment();
                        case 3 -> new CancelledEntrantsFragment();
                        // This should never happen. See getItemCount.
                        default -> new Fragment();
                    };
            frag.setArguments(bundle);
            return frag;
        }

        @Override
        public int getItemCount() {
            return 4;
        }
    }
}
