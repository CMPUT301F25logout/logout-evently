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

/**
 * Fragment that displays the account details for admin viewing.
 * The admin can delete the account from this fragment.
 */
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
            String name = "Name: " + accountData.get().name();
            String email = "Email: " + accountData.get().email();
            binding.accountEmail.setText(email);
            binding.accountName.setText(name);
        });

        // Define the delete button click listener to open a dialog
        binding.delete.setOnClickListener(v -> {
            ConfirmFragmentNoInput confirmFragment = ConfirmFragmentNoInput.newInstance(
                    "Delete Account", "Are you sure you want to delete " + accountEmail);
            confirmFragment.show(getParentFragmentManager(), "confirmNoInput");
            getParentFragmentManager()
                    .setFragmentResultListener(
                            ConfirmFragmentNoInput.requestKey, this, this::onDialogConfirmClick);
        });
    }

    /**
     * The dialog closed with a confirm click.
     * Delete the Account from the AccountDB and navigate the user back to the profile list.
     * @param requestKey key of request in bundle
     * @param result confirmation result
     */
    public void onDialogConfirmClick(String requestKey, Bundle result) {
        if (!result.getBoolean(ConfirmFragmentNoInput.inputKey)) return;
        accountDB.deleteAccount(accountEmail);

        Toast.makeText(requireContext(), "Account is deleted.", Toast.LENGTH_SHORT)
                .show();

        // Navigate back to the profile list
        NavController navController = NavHostFragment.findNavController(this);
        navController.popBackStack();
    }
}
