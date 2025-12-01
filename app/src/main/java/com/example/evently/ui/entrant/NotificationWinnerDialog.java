package com.example.evently.ui.entrant;

import java.util.UUID;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import com.example.evently.R;
import com.example.evently.data.EventsDB;
import com.example.evently.data.NotificationDB;
import com.example.evently.databinding.FragmentNotifBinding;
import com.example.evently.utils.FirebaseAuthUtils;

/**
 * Dialog shown for "Winner" notifications. i.e invited to event.
 * These not only show the notification title, message etc. but also the "accept or decline" buttons.
 */
public class NotificationWinnerDialog extends DialogFragment {
    private FragmentNotifBinding binding;

    private UUID eventID;
    private String message;

    private EventsDB eventsDB = new EventsDB();
    private NotificationDB notificationDB = new NotificationDB();

    @NonNull @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        var args = getArguments();
        if (args == null) {
            // This should never happen.
            throw new IllegalArgumentException("Notification dialog opened without arguments");
        }

        final var self = FirebaseAuthUtils.getCurrentEmail();

        binding = FragmentNotifBinding.inflate(getLayoutInflater(), null, false);

        var notificationID = (UUID) args.getSerializable("id");
        assert notificationID != null;
        eventID = (UUID) args.getSerializable("eventID");
        var title = args.getString("title");
        message = args.getString("description");

        return new MaterialAlertDialogBuilder(requireContext())
                .setTitle(title)
                .setView(binding.getRoot())
                .setPositiveButton("Accept", (dialog, id) -> {
                    eventsDB.addAccepted(eventID, self);
                    notificationDB.markSeen(notificationID, self);
                })
                .setNegativeButton("Decline", (dialog, id) -> {
                    eventsDB.addCancelled(eventID, self);
                    notificationDB.markSeen(notificationID, FirebaseAuthUtils.getCurrentEmail());
                })
                .setNeutralButton("View Event", (dialog, id) -> {
                    NavHostFragment.findNavController(requireParentFragment())
                            .navigate(
                                    ViewNotificationsFragmentDirections
                                            .actionNavNotifsToEventDetails(eventID));
                })
                .create();
    }

    @Nullable @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        new EventsDB()
                .fetchEvent(eventID)
                .thenRun(eventOpt -> eventOpt.ifPresent(event -> {
                    final var msg = getString(R.string.event_dialog_msg, event.name(), message);
                    binding.notifDescription.setText(msg);
                }));
        binding.notifDescription.setText(message);
    }
}
