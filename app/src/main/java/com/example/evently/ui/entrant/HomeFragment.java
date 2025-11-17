package com.example.evently.ui.entrant;

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

import com.example.evently.databinding.FragmentEntrantHomeBinding;

/**
 * A tab layout fragment to encapsulate both "Browse events" and "View Joined events" fragments.
 * This is used as the home screen for entrant activity.
 */
public class HomeFragment extends Fragment {
    @Nullable @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        final var binding =
                FragmentEntrantHomeBinding.inflate(getLayoutInflater(), container, false);

        final TabLayout tabLayout = binding.entrantHomeTabLayout;
        final ViewPager2 viewPager = binding.entrantHomeViewPager;

        viewPager.setAdapter(new HomeFragment.Adapter(getChildFragmentManager(), getLifecycle()));

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
                    switch (position) {
                        case 0 -> tab.setText("Browse");
                        case 1 -> tab.setText("History");
                    }
                })
                .attach();

        return binding.getRoot();
    }

    /**
     * Adapter that provides the fragments for each tab.
     */
    private static class Adapter extends FragmentStateAdapter {
        public Adapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
            super(fragmentManager, lifecycle);
        }

        @NonNull @Override
        public Fragment createFragment(int position) {
            return switch (position) {
                case 0 -> new BrowseEventsFragment();
                case 1 -> new JoinedEventsFragment();
                // This should never happen. See getItemCount.
                default -> new Fragment();
            };
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }
}
