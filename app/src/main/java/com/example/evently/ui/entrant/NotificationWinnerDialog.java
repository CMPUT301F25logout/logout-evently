package com.example.evently.ui.entrant;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

public class NotificationWinnerDialog extends DialogFragment {
    @NonNull @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());

        var args = getArguments();
        if (args == null) {
            // This should never happen.
            return builder.create();
        }

        var title = args.getString("title");
        var message = args.getString("description");
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton("Accept", (dialog, id) -> {
                    // TODO (chase): Store this entrant in the accepted list for this event.
                    // TODO (chase): Store this entrant in the "seenBy" list of this notification.
                })
                .setNegativeButton("Decline", (dialog, id) -> {
                    // TODO (chase): Store this entrant in the cancelled list for this event.
                    // TODO (chase): Store this entrant in the "seenBy" list of this notification.
                });
        return builder.create();
    }
}
