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
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import com.example.evently.data.model.EventEntrants;
import com.example.evently.databinding.FragmentEventMetaBinding;
import com.example.evently.ui.model.EventViewModel;

/**
 * Fragment that displays the tabs for event participants and their locations:
 * Enrolled, Cancelled, Selected, and MapView (optional)
 * Uses ViewPager2 and TabLayout.
 */
public class EventMetaFragment extends Fragment {

    private FragmentEventMetaBinding binding;

    private EventViewModel eventViewModel;

    @Nullable @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentEventMetaBinding.inflate(getLayoutInflater(), container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final TabLayout tabLayout = binding.eventPeopleTabLayout;
        final ViewPager2 viewPager = binding.eventPeopleViewPager;
        // This is to prevent "swipe" inputs from being eaten up as tab switches.
        // The horizontal swipes need to be handled by google maps fragment itself, not viewpager.
        viewPager.setUserInputEnabled(false);

        eventViewModel = new ViewModelProvider(requireParentFragment()).get(EventViewModel.class);

        eventViewModel.getEventLive().observe(getViewLifecycleOwner(), event -> {
            // Must use the parent fragment manager so the children have access to the original view
            // model.
            viewPager.setAdapter(new EventPeopleAdapter(
                    getParentFragmentManager(),
                    getLifecycle(),
                    event.requiresLocation()));

            new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
                        switch (position) {
                            case 0 -> tab.setText("Enrolled");
                            case 1 -> tab.setText("Selected");
                            case 2 -> tab.setText("Accepted");
                            case 3 -> tab.setText("Cancelled");
                            case 4 -> tab.setText("Map");
                        }
                    })
                    .attach();
        });
    }

    /**
     * Adapter that provides the fragments for each tab.
     */
    private static class EventPeopleAdapter extends FragmentStateAdapter {

        private final boolean requiresLocation;

        public EventPeopleAdapter(
                @NonNull FragmentManager fragmentManager,
                @NonNull Lifecycle lifecycle,
                boolean requiresLocation) {
            super(fragmentManager, lifecycle);
            this.requiresLocation = requiresLocation;
        }

        @NonNull @Override
        public Fragment createFragment(int position) {
            return switch (position) {
                case 0 -> new EnrolledEntrantsFragment();
                case 1 -> new SelectedEntrantsFragment();
                case 2 -> new AcceptedEntrantsFragment();
                case 3 -> new CancelledEntrantsFragment();
                case 4 -> new EventEntrantsMapFragment();
                // This should never happen. See getItemCount.
                default -> new Fragment();
            };
        }

        @Override
        public int getItemCount() {
            return requiresLocation ? 5 : 4;
        }
    }
}
