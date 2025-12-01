package com.example.evently.ui.entrant;

import java.util.Arrays;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.GeoPoint;

import com.example.evently.R;
import com.example.evently.data.EventsDB;
import com.example.evently.databinding.FragmentEntrantEventActionsBinding;
import com.example.evently.ui.common.EventQRDialogFragment;
import com.example.evently.ui.model.EventViewModel;
import com.example.evently.utils.FirebaseAuthUtils;

/**
 * The actions button for the entrant {@link ViewEventDetailsFragment } fragment.
 * This takes care of setting up the action buttons for the entrant view.
 * MUST be used by {@link ViewEventDetailsFragment } as it requires ViewModels from there.
 */
public class EntrantEventActionsFragment extends Fragment {
    private FragmentEntrantEventActionsBinding binding;

    private EventViewModel eventViewModel;

    private FusedLocationProviderClient locationClient;

    private final EventsDB eventsDB = new EventsDB();

    // This is set to true once the event is fetched.
    private boolean requireLocation = false;

    private final String self = FirebaseAuthUtils.getCurrentEmail();

    private static final String[] LocationPermissions = new String[] {
        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
    };

    @SuppressLint("MissingPermission")
    private final ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestMultiplePermissions(), grantMap -> {
                        if (grantMap.containsValue(false)) {
                            Toast.makeText(
                                            requireContext(),
                                            "This event requires location to enroll",
                                            Toast.LENGTH_LONG)
                                    .show();
                        } else {
                            // We must have been launched from within enroll, continue that flow.
                            enrollWithLocation();
                        }
                    });

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentEntrantEventActionsBinding.inflate(getLayoutInflater(), container, false);

        eventViewModel = new ViewModelProvider(requireParentFragment()).get(EventViewModel.class);
        locationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.lotteryGuidelinesButton.setOnClickListener(
                v -> new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.lottery_guidelines_dialog_title)
                        .setMessage(R.string.lottery_guidelines_dialog_message)
                        .setPositiveButton(
                                R.string.lottery_guidelines_dialog_positive,
                                (dialog, which) -> dialog.dismiss())
                        .show());

        eventViewModel.getEventLive().observe(getViewLifecycleOwner(), event -> {
            requireLocation = event.requiresLocation();
            if (event.isFull()) {
                // Disable the button if the waitlist is already full
                binding.waitlistAction.setEnabled(false);
                // Change the button text to indicate that it's full
                binding.waitlistAction.setText(R.string.event_join_btn_full);
            } else {
                binding.waitlistAction.setEnabled(true);
            }
        });

        eventViewModel.getEventEntrantsLive().observe(getViewLifecycleOwner(), eventEntrants -> {
            if (eventEntrants.all().contains(self)) {
                // The user has already joined!
                binding.waitlistAction.setText(R.string.event_join_btn_joined);
                binding.waitlistAction.setOnClickListener(
                        v -> eventsDB.unenroll(eventViewModel.eventID, self).thenRun(vu -> {
                            // A join/leave action has happened - ask to update entrants and event.
                            eventViewModel.requestUpdate();
                        }));
            } else {
                // The user may join.
                binding.waitlistAction.setText(R.string.event_join_btn);
                binding.waitlistAction.setOnClickListener(v -> {
                    binding.waitlistAction.setEnabled(false);
                    // Check if enrollment requires location.
                    // N.B: This relies on the eventViewModel observer firing first.
                    if (requireLocation) {
                        if (Arrays.stream(LocationPermissions)
                                .anyMatch(perm ->
                                        ContextCompat.checkSelfPermission(requireContext(), perm)
                                                != PackageManager.PERMISSION_GRANTED)) {
                            // Directly ask for the permission as they aren't granted yet.
                            requestPermissionLauncher.launch(new String[] {
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            });
                        } else {
                            // Already have permission, go ahead with enrolling.
                            enrollWithLocation();
                        }
                    } else {
                        enrollIntoEvent(null);
                    }
                });
            }
        });

        binding.utilShareBtn.shareBtn.setOnClickListener(v -> {
            final var qrDialog = new EventQRDialogFragment();
            final var bundle = new Bundle();
            bundle.putSerializable("eventID", eventViewModel.eventID);
            qrDialog.setArguments(bundle);
            qrDialog.show(getChildFragmentManager(), "QR_DIALOG");
        });
    }

    // This must only be called once we know the location permission has been granted.
    @RequiresPermission(
            allOf = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            })
    private void enrollWithLocation() {
        locationClient
                .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(location -> {
                    enrollIntoEvent(new GeoPoint(location.getLatitude(), location.getLongitude()));
                });
    }

    private void enrollIntoEvent(GeoPoint loc) {
        assert eventViewModel.eventID != null;
        eventsDB.enroll(eventViewModel.eventID, self, loc)
                .thenRun(vu -> {
                    binding.waitlistAction.setEnabled(true);
                    // A join/leave action has happened - ask to update entrants and event.
                    eventViewModel.requestUpdate();
                })
                .catchE(e -> {
                    switch (e) {
                        case IllegalArgumentException ignored ->
                            Toast.makeText(requireContext(), "Waitlist is full", Toast.LENGTH_SHORT)
                                    .show();
                        case IllegalStateException ignored ->
                            Toast.makeText(
                                            requireContext(),
                                            "Enrollment deadline has passed",
                                            Toast.LENGTH_SHORT)
                                    .show();
                        default -> {
                            Log.e("EntrantEventActions", e.toString());
                            Toast.makeText(
                                            requireContext(),
                                            "Something went wrong...",
                                            Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }
                    binding.waitlistAction.setEnabled(true);
                    eventViewModel.requestUpdate();
                });
    }
}
