package com.example.evently.ui.organizer;

import android.content.Intent;
import android.os.Bundle;

import com.example.evently.databinding.ActivityOrganizerBinding;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import android.view.View;

import com.example.evently.R;

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
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(fragmentContainer.getId());
        assert navHostFragment != null;
        NavController navController = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(navBar, navController);
    }
}