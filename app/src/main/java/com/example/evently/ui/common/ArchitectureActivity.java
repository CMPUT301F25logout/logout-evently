package com.example.evently.ui.common;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NavigationRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.firebase.messaging.FirebaseMessaging;

import com.example.evently.databinding.ActivityArchitectureBinding;
import com.example.evently.utils.FirebaseMessagingUtils;

/**
 * Abstract class that is the heart of all core activities of Evently: EntrantActivity, OrganizerActivity etc.
 * <p>
 * Extending activities must implement getGraph to attach their own navigation graphs.
 * This allows each activity to have their own fragments to differentiate each other, while
 * sharing common functionality.
 * @see com.example.evently.ui.organizer.OrganizerActivity
 */
public abstract class ArchitectureActivity extends AppCompatActivity {
    private ActivityArchitectureBinding binding;
    protected NavController navController;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(), isGranted -> {
                        if (!isGranted) {
                            Toast.makeText(
                                            this,
                                            "You will not receive notifications regarding events",
                                            Toast.LENGTH_LONG)
                                    .show();
                        }
                    });

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
        navController = navHostFragment.getNavController();
        navController.setGraph(this.getGraph());
        NavigationUI.setupWithNavController(navBar, navController, false);

        askNotificationPermission();

        // Get the currently usable token and update it in Database if need be.
        // This is a setup that needs to exist at least once every time the app starts (and
        // authenticates).
        FirebaseMessaging.getInstance()
                .getToken()
                .addOnSuccessListener(FirebaseMessagingUtils::storeToken);
        // TODO (chase): Maybe also schedule a periodic task that refreshes the token?
        // See: https://firebase.google.com/docs/cloud-messaging/manage-tokens

        setupSwitchRoleButton(binding.btnSwitchRole);
    }

    /**
     * Configures the “Switch Role” button for the current Activity.
     * <p>
     * If called from {@code EntrantActivity}, the button text is set to
     * "Switch to Organizer” and tapping it starts {@code OrganizerActivity}.
     * If called from {@code OrganizerActivity}, the button text is set to
     * “Switch to Entrant” and tapping it starts {@code EntrantActivity}.
     * If invoked from any other Activity type, the button is hidden.
     * </p>
     * @param b the button to configure; may be {@code null}.
     *
     */
    private void setupSwitchRoleButton(Button b) {
        if (b == null) return;

        if (this instanceof com.example.evently.ui.entrant.EntrantActivity) {
            b.setText("Switch to Organizer");
            b.setOnClickListener(_x -> {
                Intent i =
                        new Intent(this, com.example.evently.ui.organizer.OrganizerActivity.class);
                startActivity(i);
            });
        } else if (this instanceof com.example.evently.ui.organizer.OrganizerActivity) {
            b.setText("Switch to Entrant");
            b.setOnClickListener(_x -> {
                Intent i = new Intent(this, com.example.evently.ui.entrant.EntrantActivity.class);
                startActivity(i);
            });
        } else {
            // Unknown role: hide
            b.setOnClickListener(null);
            b.setVisibility(android.view.View.GONE);
        }
    }

    /**
     * Handles the Action Bar "Up" button press.
     * <p>
     * Delegates to the Activity's {@link androidx.navigation.NavController} first; if it
     * can navigate up in its back stack, that result is used. Otherwise, falls back to the
     * default {@link androidx.appcompat.app.AppCompatActivity#onSupportNavigateUp()} behavior.
     * @return true if the NavController (or the super implementation) handled the
     * navigation or false otherwise.
     */
    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }

    /**
     *
     */
    private void askNotificationPermission() {
        // This is only necessary for API Level > 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }
}
