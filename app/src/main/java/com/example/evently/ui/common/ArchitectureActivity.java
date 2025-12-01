package com.example.evently.ui.common;

import static com.example.evently.data.model.Role.AdminRole;
import static com.example.evently.data.model.Role.EntrantRole;
import static com.example.evently.data.model.Role.OrganizerRole;

import java.util.List;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
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

import com.example.evently.data.AccountDB;
import com.example.evently.data.model.Role;
import com.example.evently.databinding.ActivityArchitectureBinding;
import com.example.evently.ui.admin.AdminActivity;
import com.example.evently.ui.auth.AuthActivity;
import com.example.evently.ui.entrant.EntrantActivity;
import com.example.evently.ui.organizer.OrganizerActivity;
import com.example.evently.utils.FirebaseAuthUtils;
import com.example.evently.utils.FirebaseMessagingUtils;

/**
 * Abstract class that is the heart of all core activities of Evently: EntrantActivity, OrganizerActivity etc.
 * <p>
 * Extending activities must implement getGraph to attach their own navigation graphs.
 * This allows each activity to have their own fragments to differentiate each other, while
 * sharing common functionality.
 * <p>
 * Extending activities should take care to put good labels on their navigation graph destinations.
 * This label is used as the title for the screen (top left).
 * @implNote The activity implements OnItemSelectedListener meant for the role selector spinner.
 * @see com.example.evently.ui.organizer.OrganizerActivity
 */
public abstract class ArchitectureActivity extends AppCompatActivity
        implements AdapterView.OnItemSelectedListener {
    private ActivityArchitectureBinding binding;
    protected NavController navController;

    private static Role[] DefaultRoles = new Role[] {EntrantRole, OrganizerRole};
    // Track the role selected by the role spinner so we can avoid switching when we're already
    // there!
    private int currentlySelectedRole = rolePosition();

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

    /**
     * Role index for the implementing activity.
     * @implNote Entrant=0, Organizer=1, Admin>1
     */
    protected int rolePosition() {
        return switch (this) {
            case EntrantActivity ignored -> 0;
            case OrganizerActivity ignored -> 1;
            case AdminActivity ignored -> 2;
            // Admin activity.
            default -> 3;
        };
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityArchitectureBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Attach the role selector adapter to the spinner.
        final var availableRoles = List.of(DefaultRoles);
        // Add the admin role if logged in account is an admin.
        new AccountDB().isAdmin(FirebaseAuthUtils.getCurrentEmail()).thenRun(isAdmin -> {
            if (isAdmin) {
                availableRoles.add(AdminRole);
            }
            binding.roleSelector.setAdapter(new RoleSpinnerAdapter(this, availableRoles));
            binding.roleSelector.setOnItemSelectedListener(this);
            binding.roleSelector.setSelection(rolePosition());
        });

        // Set the navbar.
        final var navBar = binding.navbar;
        final var fragmentContainer = binding.navHostFragment;
        NavHostFragment navHostFragment = (NavHostFragment)
                getSupportFragmentManager().findFragmentById(fragmentContainer.getId());
        assert navHostFragment != null;
        navController = navHostFragment.getNavController();
        navController.setGraph(this.getGraph());
        NavigationUI.setupWithNavController(navBar, navController, false);
        navController.addOnDestinationChangedListener((navController, destination, args) -> {
            // Set the title
            if (destination.getLabel() != null) {
                binding.homeTitle.setText(destination.getLabel());
            }
        });

        askNotificationPermission();

        // Get the currently usable token and update it in Database if need be.
        // This is a setup that needs to exist at least once every time the app starts (and
        // authenticates).
        FirebaseMessaging.getInstance()
                .getToken()
                .addOnSuccessListener(FirebaseMessagingUtils::storeToken);
        // TODO (chase): Maybe also schedule a periodic task that refreshes the token?
        // See: https://firebase.google.com/docs/cloud-messaging/manage-tokens

        // Attach listener for edit profile sign out/delete account action.
        navHostFragment
                .getChildFragmentManager()
                .setFragmentResultListener(
                        EditProfileFragment.resultTag, this, (result, resultBundle) -> {
                            // Disable FCM auto init and remove the token so the device no longer
                            // gets notifications.
                            FirebaseMessaging.getInstance().setAutoInitEnabled(false);
                            FirebaseMessaging.getInstance()
                                    .deleteToken()
                                    .addOnSuccessListener(e -> {
                                        final var intent = new Intent(
                                                ArchitectureActivity.this, AuthActivity.class);
                                        startActivity(intent);
                                        finish();
                                    });
                        });
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
     * Set up notification permission request for the user to approve.
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

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (currentlySelectedRole == position) {
            // Nothing to do.
            return;
        }
        final var intent =
                switch (position) {
                    case 0 -> new Intent(this, EntrantActivity.class);
                    case 1 -> new Intent(this, OrganizerActivity.class);
                    case 2 -> new Intent(this, AdminActivity.class);
                    // TODO (chase): Admin activity intent here.
                    default -> throw new RuntimeException("Admin activity not implemented yet");
                };
        currentlySelectedRole = position;
        startActivity(intent);
        finish();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // This is unlikely to be called for a spinner.
        currentlySelectedRole = rolePosition();
    }
}
