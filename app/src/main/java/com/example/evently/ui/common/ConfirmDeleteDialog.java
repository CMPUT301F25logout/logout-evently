package com.example.evently.ui.common;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

/**
 * Classes that use this confirm dialog need to implement ConfirmDeleteDialog.ConfirmDeleteListener
 * Make these two methods -> onDialogConfirmClick() and onDialogCancelClick()
 */
public class ConfirmDeleteDialog extends DialogFragment {

    public interface ConfirmDeleteListener {
        public void onDialogConfirmClick(DialogFragment dialog);

        public void onDialogCancelClick(DialogFragment dialog);
    }

    ConfirmDeleteListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (ConfirmDeleteListener) getParentFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException(e.toString());
        }
    }

    @NonNull @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());

        var args = getArguments();
        if (args == null) {
            return builder.create();
        }

        // Get the title and message arguments
        String title = args.getString("title");
        String message = args.getString("message");

        // Build the dialog
        builder.setTitle(title)
                .setMessage(message)
                // Positive button
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        listener.onDialogConfirmClick(ConfirmDeleteDialog.this);
                    }
                })
                // Negative button should just close out the dialog
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        listener.onDialogCancelClick(ConfirmDeleteDialog.this);
                    }
                });

        return builder.create();
    }
}
