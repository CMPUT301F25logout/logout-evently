package com.example.evently.ui.entrant;

import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

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

        binding.lotteryGuidelinesButton.setOnClickListener(v -> {
            final var dialog = new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.lottery_guidelines_dialog_title)
                    .setMessage(
                            HtmlCompat.fromHtml(
                                    getString(R.string.lottery_guidelines_dialog_message),
                                    HtmlCompat.FROM_HTML_MODE_LEGACY))
                    .setPositiveButton(
                            R.string.lottery_guidelines_dialog_positive,
                            (dialogInterface, which) -> dialogInterface.dismiss())
                    .show();

            final var messageView = (TextView) dialog.findViewById(android.R.id.message);
            if (messageView != null) {
                messageView.setMovementMethod(LinkMovementMethod.getInstance());
            }
        });


        eventViewModel.getEventLive().observe(getViewLifecycleOwner(), event -> {
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
                binding.waitlistAction.setOnClickListener(v -> eventsDB.enroll(
                                eventViewModel.eventID, self)
                        .thenRun(vu -> {
                            // A join/leave action has happened - ask to update entrants and event.
                            eventViewModel.requestUpdate();
                        })
                        .catchE(e -> {
                            switch (e) {
                                case IllegalArgumentException ignored ->
                                    Toast.makeText(
                                                    requireContext(),
                                                    "Waitlist is full",
                                                    Toast.LENGTH_SHORT)
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
                            eventViewModel.requestUpdate();
                        }));
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
}
