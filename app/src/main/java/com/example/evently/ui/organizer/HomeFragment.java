package com.example.evently.ui.organizer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.evently.databinding.FragmentOrganizerHomeBinding;

/**
 * Home page for organizers. This holds the events they created as well as a button to create more events.
 */
public class HomeFragment extends Fragment {
    private FragmentOrganizerHomeBinding binding;

    @Nullable @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentOrganizerHomeBinding.inflate(getLayoutInflater(), container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnCreateEvent.setOnClickListener(v -> {
            var action = HomeFragmentDirections.actionNavHomeToCreateEvent();
            NavController navController = NavHostFragment.findNavController(this);
            navController.navigate(action);
        });
    }
}
