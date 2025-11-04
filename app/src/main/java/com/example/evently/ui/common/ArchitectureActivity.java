package com.example.evently.ui.common;

import android.os.Bundle;
import androidx.annotation.NavigationRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.evently.databinding.ActivityArchitectureBinding;

/**
 * Abstract class that is the heart of all core activities of Evently: EntrantActivity, OrganizerActivity etc.
 * <p>
 * Extending activities must implement getGraph to attach their own navigation graphs.
 * This allows each activity to have their own fragments to differentiate each other, while
 * sharing common functionality.
 * @see com.example.evently.ui.organizer.OrganizerActivity
 */
public abstract class ArchitectureActivity extends AppCompatActivity {

    //
    private ActivityArchitectureBinding binding;

    /**
     * This method allows "navigation polymorphism" between implementing activities.
     * @return The navigation graph used by the implementing activity.
     */
    @NavigationRes
    protected abstract int getGraph();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityArchitectureBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set the navbar.
        final var navBar = binding.includeNavbar.navbar;
        final var fragmentContainer = binding.navHostFragment;
        NavHostFragment navHostFragment = (NavHostFragment)
                getSupportFragmentManager().findFragmentById(fragmentContainer.getId());
        assert navHostFragment != null;
        NavController navController = navHostFragment.getNavController();
        navController.setGraph(this.getGraph());
        NavigationUI.setupWithNavController(navBar, navController);
    }
}
