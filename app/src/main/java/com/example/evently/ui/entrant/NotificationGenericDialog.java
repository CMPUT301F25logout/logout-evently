package com.example.evently.ui.entrant;

import java.util.UUID;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.evently.data.EventsDB;
import com.example.evently.data.NotificationDB;
import com.example.evently.utils.FirebaseAuthUtils;

/**
 * Generic notification dialog used for all non-winner notifications.
 * These simply have an "ok" button. No further action is required.
 */
public class NotificationGenericDialog extends DialogFragment {
    @NonNull @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());

        var args = getArguments();
        if (args == null) {
            // This should never happen.
            return builder.create();
        }

        var notificationID = (UUID) args.getSerializable("id");
        assert notificationID != null;
        var eventID = (UUID) args.getSerializable("eventID");
        var title = args.getString("title");
        var message = args.getString("description");
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, id) -> new NotificationDB()
                        .markSeen(notificationID, FirebaseAuthUtils.getCurrentEmail()))
                .setNeutralButton("See Details", (dialog, id) -> {
                    if (eventID != null) {
                        NavHostFragment.findNavController(requireParentFragment())
                                .navigate(
                                        ViewNotificationsFragmentDirections
                                                .actionNavNotifsToEventDetails(eventID));
                    }
                    new NotificationDB()
                            .markSeen(notificationID, FirebaseAuthUtils.getCurrentEmail());
                });

        final var alertDialog = builder.create();

        if (eventID != null) {
            new EventsDB()
                    .fetchEvent(eventID)
                    .thenRun(eventOpt -> eventOpt.ifPresent(event -> requireActivity()
                            .runOnUiThread(() -> alertDialog.setMessage(
                                    "You have been invited to the event:\n" + event.name()))));
        }

        return alertDialog;
    }
}
