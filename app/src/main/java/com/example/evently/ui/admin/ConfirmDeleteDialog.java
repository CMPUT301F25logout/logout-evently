package com.example.evently.ui.admin;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.evently.data.AccountDB;

public class ConfirmDeleteDialog extends DialogFragment {
    @NonNull @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        final AccountDB accountDB = new AccountDB();

        var args = getArguments();
        if (args == null) {
            return builder.create();
        }

        // Get the arguments
        String accountEmail = (String) args.getSerializable("accountEmail");
        assert accountEmail != null;
        String title = args.getString("title");
        String message = args.getString("message");

        // Build the dialog
        builder.setTitle(title)
                .setMessage(message)
                // Confirm deletion
                .setPositiveButton("Confirm", (dialog, id) -> {
                    // Delete account from other events?
                    accountDB.deleteAccount(accountEmail);
                })
                .setNegativeButton("Cancel", (dialog, id) -> {
                    // Close the dialog
                });

        return builder.create();
    }
}
