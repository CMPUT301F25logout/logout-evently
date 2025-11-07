package com.example.evently.ui.entrant;

import java.util.UUID;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.example.evently.data.EventsDB;
import com.example.evently.data.NotificationDB;
import com.example.evently.utils.FirebaseAuthUtils;

/**
 * Dialog shown for "Winner" notifications. i.e invited to event.
 * These not only show the notification title, message etc. but also the "accept or decline" buttons.
 */
public class NotificationWinnerDialog extends DialogFragment {
    @NonNull @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        final var eventsDB = new EventsDB();
        final var notificationDB = new NotificationDB();
        final var self = FirebaseAuthUtils.getCurrentEmail();

        var args = getArguments();
        if (args == null) {
            // This should never happen.
            return builder.create();
        }

        var notificationID = (UUID) args.getSerializable("id");
        var eventID = (UUID) args.getSerializable("eventID");
        assert notificationID != null;
        var title = args.getString("title");
        var message = args.getString("description");
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton("Accept", (dialog, id) -> {
                    eventsDB.addAccepted(eventID, self);
                    notificationDB.markSeen(notificationID, self);
                })
                .setNegativeButton("Decline", (dialog, id) -> {
                    eventsDB.addCancelled(eventID, self);
                    notificationDB.markSeen(notificationID, FirebaseAuthUtils.getCurrentEmail());
                });
        return builder.create();
    }
}
