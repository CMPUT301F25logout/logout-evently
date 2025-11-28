package com.example.evently.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.evently.data.AccountDB;
import com.example.evently.databinding.FragmentAdminProfileBinding;
import com.example.evently.ui.common.ConfirmFragmentNoInput;

public class ViewProfileDetailsFragment extends Fragment {
    private FragmentAdminProfileBinding binding;

    private final AccountDB accountDB = new AccountDB();

    private String accountEmail;

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
        accountDB.fetchAccount(accountEmail).thenRun(accountData -> {
            binding.accountEmail.setText(accountData.get().email());
            binding.accountName.setText(accountData.get().name());
        });

        // Define the delete button click listener to open a dialog
        binding.delete.setOnClickListener(v -> {
            ConfirmFragmentNoInput confirmFragment = ConfirmFragmentNoInput.newInstance(
                    "Delete Account", "Are you sure you want to delete " + accountEmail);
            confirmFragment.show(getParentFragmentManager(), "confirmNoInput");
            getParentFragmentManager()
                    .setFragmentResultListener(
                            ConfirmFragmentNoInput.requestKey, this, this::onDialogConfirmClick);
            /*
            DialogFragment dialog = new ConfirmDeleteDialog();

            // Make a bundle to send args to the dialog
            String title = "Delete Account";
            String message = "Are you sure you want to delete " + accountEmail;
            Bundle args = new Bundle();
            args.putString("title", title);
            args.putString("message", message);
            dialog.setArguments(args);
            dialog.show(getParentFragmentManager(), "ConfirmDeleteAccountDialog");
             */
        });
    }
    /**
     * The dialog closed with a positive click (confirm).
     * Delete the Account from the AccountDB and navigate the user back to the profile list.
     */
    public void onDialogConfirmClick(String s, Bundle bundle) {
        if (!bundle.getBoolean(ConfirmFragmentNoInput.inputKey)) return;
        accountDB.deleteAccount(accountEmail);

        Toast.makeText(requireContext(), "Account is deleted.", Toast.LENGTH_SHORT)
                .show();

        // Navigate back to the profile list
        NavController navController = NavHostFragment.findNavController(this);
        navController.popBackStack();
    }
}
