package com.example.evently.ui.entrant;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

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

    private final EventsDB eventsDB = new EventsDB();

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentEntrantEventActionsBinding.inflate(getLayoutInflater(), container, false);

        eventViewModel = new ViewModelProvider(requireParentFragment()).get(EventViewModel.class);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final var self = FirebaseAuthUtils.getCurrentEmail();

        eventViewModel.getEventEntrantsLive().observe(getViewLifecycleOwner(), eventEntrants -> {
            binding.waitlistAction.setEnabled(true);
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
                binding.waitlistAction.setOnClickListener(
                        v -> eventsDB.enroll(eventViewModel.eventID, self).thenRun(vu -> {
                            // A join/leave action has happened - ask to update entrants and event.
                            eventViewModel.requestUpdate();
                        }));
            }
        });

        //        switch (waitlistStatus) {
        //            case WaitlistStatus.FULL -> {
        //                // Disable the button if the waitlist is already full
        //                binding.waitlistAction.setEnabled(false);
        //                // Change the button text to indicate that it's full
        //                binding.waitlistAction.setText(R.string.event_join_btn_full);
        //            }

        binding.utilShareBtn.shareBtn.setOnClickListener(v -> {
            final var qrDialog = new EventQRDialogFragment();
            final var bundle = new Bundle();
            bundle.putSerializable("eventID", eventViewModel.eventID);
            qrDialog.setArguments(bundle);
            qrDialog.show(getChildFragmentManager(), "QR_DIALOG");
        });
    }
}
