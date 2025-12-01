package com.example.evently.ui.common;

import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.evently.R;
import com.example.evently.data.AccountDB;
import com.example.evently.utils.FirebaseAuthUtils;
import com.example.evently.utils.TextInputValidator;

/**
 * Fragment for editing profile
 * Connected to fragment_edit_profile.xml
 */
public class EditProfileFragment extends Fragment {
    public static final String resultTag = "SignOut";

    private AccountDB db;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        db = new AccountDB();
        return inflater.inflate(R.layout.fragment_edit_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        String accountEmail = FirebaseAuthUtils.getCurrentEmail();

        initInfo(v, accountEmail);
        connectEditName(v, accountEmail);
        connectEditEmail(v, accountEmail);
        connectEditPhone(v, accountEmail);
        connectSignOut(v);
        connectDeleteAccount(v);
    }

    /**
     * Fill edit profile page fields with info from db
     * @param v edit profile page view
     * @param accountEmail email of account
     */
    private void initInfo(View v, String accountEmail) {
        TextView headerView = v.findViewById(R.id.header);
        TextView nameView = v.findViewById(R.id.name_text);
        TextView emailView = v.findViewById(R.id.email_text);
        TextView phoneView = v.findViewById(R.id.phone_text);

        db.fetchAccount(accountEmail).optionally(account -> {
            emailView.setText(account.visibleEmail());
            nameView.setText(account.name());

            account.phoneNumber()
                    .ifPresentOrElse(
                            n -> phoneView.setText(formatPhoneNumber(n)),
                            () -> phoneView.setText("None"));
            headerView.setText(String.format("%s's Profile", account.name()));
        });
    }

    /**
     * Connect edit name button to confirmFragmentTextInput and name view
     * @param v edit profile page view
     * @param accountEmail email of account
     */
    private void connectEditName(View v, String accountEmail) {
        TextView headerView = v.findViewById(R.id.header);
        TextView nameView = v.findViewById(R.id.name_text);
        Button editName = v.findViewById(R.id.name_button);

        editName.setOnClickListener(view -> {
            final var confirmFragment = ConfirmFragmentTextInput.newInstance(
                    "Change Name",
                    "Please enter your new name below.",
                    "Jane Doe",
                    TextInputValidator.NAME);
            confirmFragment.show(getParentFragmentManager(), "confirmTextInput");

            getParentFragmentManager()
                    .setFragmentResultListener(
                            ConfirmFragmentTextInput.requestKey, this, (requestKey, result) -> {
                                String newName =
                                        result.getString(ConfirmFragmentTextInput.inputKey);
                                db.updateName(accountEmail, newName);
                                nameView.setText(newName);
                                headerView.setText(String.format("%s's Profile", newName));
                            });
        });
    }

    /**
     * Connect edit email button to confirmFragmentTextInput and email view
     * @param v edit profile page view
     * @param accountEmail email of account
     */
    private void connectEditEmail(View v, String accountEmail) {
        TextView emailView = v.findViewById(R.id.email_text);
        Button editEmail = v.findViewById(R.id.email_button);

        editEmail.setOnClickListener(view -> {
            final var confirmFragment = ConfirmFragmentTextInput.newInstance(
                    "Change Email",
                    "Please enter your new email below.",
                    "Sample@example.com",
                    TextInputValidator.EMAIL);
            confirmFragment.show(getParentFragmentManager(), "confirmTextInput");

            getParentFragmentManager()
                    .setFragmentResultListener(
                            ConfirmFragmentTextInput.requestKey, this, (requestKey, result) -> {
                                String newEmail =
                                        result.getString(ConfirmFragmentTextInput.inputKey);
                                db.updateVisibleEmail(accountEmail, newEmail);
                                emailView.setText(newEmail);
                            });
        });
    }

    /**
     * Connect edit phone button to confirmFragmentTextInput and phone view
     * @param v edit profile page view
     * @param accountEmail email of account
     */
    private void connectEditPhone(View v, String accountEmail) {
        TextView phoneView = v.findViewById(R.id.phone_text);
        Button editPhone = v.findViewById(R.id.phone_button);
        editPhone.setOnClickListener(view -> {
            final var confirmFragment = ConfirmFragmentTextInput.newInstance(
                    "Change Phone Number",
                    "Please enter your new phone number below.",
                    "(000) 000-0000",
                    TextInputValidator.PHONE);
            confirmFragment.show(getParentFragmentManager(), "confirmTextInput");

            getParentFragmentManager()
                    .setFragmentResultListener(
                            ConfirmFragmentTextInput.requestKey, this, (requestKey, result) -> {
                                String number = result.getString(ConfirmFragmentTextInput.inputKey);
                                if (number != null) number = formatPhoneNumber(number);
                                if (number.equals("None")) {
                                    Toast.makeText(
                                                    requireContext(),
                                                    "Invalid Number",
                                                    Toast.LENGTH_SHORT)
                                            .show();
                                    return;
                                }
                                db.updatePhoneNumber(accountEmail, number);
                                phoneView.setText(number);
                            });
        });
    }

    /**
     * Connect sign out button to Sign Out functionality
     * @param v edit profile page view
     */
    private void connectSignOut(View v) {
        Button signOut = v.findViewById(R.id.sign_out);

        signOut.setOnClickListener(view -> {
            ConfirmFragmentNoInput confirmFragment = ConfirmFragmentNoInput.newInstance(
                    "CONFIRM SIGNING OUT", "This action will sign you out!");
            confirmFragment.show(getParentFragmentManager(), "confirmNoInput");
            getParentFragmentManager()
                    .setFragmentResultListener(
                            ConfirmFragmentNoInput.requestKey, this, this::signOutListener);
        });
    }

    /**
     * Connect delete account button to delete account functionality
     * @param v edit profile page view
     */
    private void connectDeleteAccount(View v) {
        Button deleteAccount = v.findViewById(R.id.delete_account);

        deleteAccount.setOnClickListener(view -> {
            ConfirmFragmentNoInput confirmFragment = ConfirmFragmentNoInput.newInstance(
                    "DELETE ACCOUNT",
                    "Are you sure you want to delete your account? This will log you out!");
            confirmFragment.show(getParentFragmentManager(), "confirmNoInput");
            getParentFragmentManager()
                    .setFragmentResultListener(
                            ConfirmFragmentNoInput.requestKey, this, this::deleteAccountListener);
        });
    }

    /**
     * Manage result of sign out confirmation
     * @param requestKey key of request in bundle
     * @param result confirmation result
     */
    private void signOutListener(String requestKey, Bundle result) {
        if (!result.getBoolean(ConfirmFragmentNoInput.inputKey)) return;
        FirebaseAuthUtils.signOut();
        // Let the parent activity know that it's time to sign out.
        getParentFragmentManager().setFragmentResult(resultTag, new Bundle());
    }

    /**
     * Manage result of delete account confirmation
     * @param requestKey key of request in bundle
     * @param result confirmation result
     */
    private void deleteAccountListener(String requestKey, Bundle result) {
        if (!result.getBoolean(ConfirmFragmentNoInput.inputKey)) return;
        FirebaseAuthUtils.deleteAccount(
                requireActivity(),
                task -> {
                    // Let the parent activity know that it's time to sign out.
                    getParentFragmentManager().setFragmentResult(resultTag, new Bundle());
                },
                e -> {
                    Log.w("EditProfileFragment", "Unable to delete account: ", e);
                });
    }

    /**
     * Get the phone number in format to be displayed
     * @param unformattedNumber phone number pre-formatting
     * @return String formatted phone number as (000) 000-0000
     */
    private String formatPhoneNumber(String unformattedNumber) {
        if (unformattedNumber.isBlank()) return "";
        if (!Patterns.PHONE.matcher(unformattedNumber).matches()) return "None";
        String phoneNum = unformattedNumber.replaceAll("\\D", "");
        if (phoneNum.length() < 10) return "None";
        return String.format(
                "(%s) %s-%s",
                phoneNum.substring(0, 3), phoneNum.substring(3, 6), phoneNum.substring(6, 10));
    }
}
