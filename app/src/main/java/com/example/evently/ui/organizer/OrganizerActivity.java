package com.example.evently.ui.organizer;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.evently.databinding.ActivityOrganizerBinding;

public class OrganizerActivity extends AppCompatActivity {

    private ActivityOrganizerBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityOrganizerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set the navbar.
        final var navBar = binding.includeNavbar.navbar;
        final var fragmentContainer = binding.navHostFragment;
        NavHostFragment navHostFragment = (NavHostFragment)
                getSupportFragmentManager().findFragmentById(fragmentContainer.getId());
        assert navHostFragment != null;
        NavController navController = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(navBar, navController);
    }
}
