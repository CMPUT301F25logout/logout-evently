package com.example.evently.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.example.evently.data.AccountDB;
import com.example.evently.data.model.Account;
import com.example.evently.databinding.FragmentAdminProfileBinding;

public class ViewProfileDetailsFragment extends Fragment {
    private FragmentAdminProfileBinding binding;

    private final AccountDB accountDB = new AccountDB();

    private String accountEmail;

    private Account account;

    @Nullable @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminProfileBinding.inflate(getLayoutInflater(), container, false);

        // Get the email from the argument
        Bundle bundle = getArguments();
        ViewProfileDetailsFragmentArgs args = ViewProfileDetailsFragmentArgs.fromBundle(bundle);
        accountEmail = args.getAccountEmail();
        assert accountEmail != null;

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Fetch the account information and define the information
        accountDB.fetchAccount(accountEmail).optionally(accountData -> {
            binding.accountEmail.setText(accountData.email());
            binding.accountName.setText(accountData.name());
            account = accountData;
        });

        // Define the delete button click listener to open a dialog
        binding.delete.setOnClickListener(v -> {
            DialogFragment dialog = new ConfirmDeleteDialog();

            // Make a bundle to send args to the dialog
            String title = "Delete Account";
            String message = "Are you sure you want to delete " + account.name();
            Bundle args = new Bundle();
            args.putString("title", title);
            args.putString("title", message);
            args.putString("accountEmail", accountEmail);
            dialog.setArguments(args);
            // TODO Define click listeners to implement deleting logic here instead so that
            // TODO navigating back to the BrowseProfilesFragment is possible

            dialog.show(getParentFragmentManager(), "ConfirmDeleteAccountDialog");
        });
    }
}
