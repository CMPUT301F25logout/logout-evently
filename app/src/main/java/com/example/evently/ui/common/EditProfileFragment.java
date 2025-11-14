package com.example.evently.ui.common;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.evently.R;
import com.example.evently.data.AccountDB;
import com.example.evently.data.model.Account;
import com.example.evently.data.model.Category;
import com.example.evently.data.model.Event;
import com.example.evently.ui.auth.SignOutFragment;
import com.example.evently.utils.FirebaseAuthUtils;
import com.google.firebase.Timestamp;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Optional;

public class EditProfileFragment extends Fragment {

    private AccountDB db;

    /**
     * Inflates the "Create Event" form
     *
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return the view of the inflated form
     */
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        db = new AccountDB();
        return inflater.inflate(R.layout.fragment_edit_profile, container, false);
    }

    /**
     * Builds up a form, validates input, builds an {@link Event}, returns it, and navigates up
     *
     * @param v The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here
     */
    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        TextView headerView = v.findViewById(R.id.edit_profile_header);
        TextView nameView = v.findViewById(R.id.edit_profile_name_text);
        TextView emailView = v.findViewById(R.id.edit_profile_email_text);
        TextView phoneView = v.findViewById(R.id.edit_profile_phone_text);
        Button editEmail = v.findViewById(R.id.edit_profile_email_button);
        Button editPhone = v.findViewById(R.id.edit_profile_phone_button);
        Button deleteAccount = v.findViewById(R.id.edit_profile_delete_account_button);
        Button signOut = v.findViewById(R.id.edit_profile_sign_out_button);

        String accountEmail = FirebaseAuthUtils.getCurrentEmail();
        db.fetchAccount(accountEmail).optionally(account -> {
            emailView.setText(account.visibleEmail());
            nameView.setText(account.name());
            phoneView.setText(account.phoneNumber().orElse("None"));
            headerView.setText(account.name() + "'s profile");
        });

        editEmail.setOnClickListener(view -> {
            ConfirmFragmentTextInput confirmFragment = ConfirmFragmentTextInput.newInstance(
                    "Change Email",
                    "Please enter your new email below.",
                    "New Email");
            confirmFragment.show(getParentFragmentManager(), "confirmTextInput");
            getParentFragmentManager().setFragmentResultListener(ConfirmFragmentTextInput.requestKey, this, (requestKey, result) -> {
                String newEmail = result.getString(ConfirmFragmentTextInput.inputKey);
                db.updateVisibleEmail(accountEmail, newEmail);
                emailView.setText(newEmail);
            });
        });

        editPhone.setOnClickListener(view -> {
            ConfirmFragmentTextInput confirmFragment = ConfirmFragmentTextInput.newInstance(
                    "Change Phone Number",
                    "Please enter your new phone number below.",
                    "000-000-0000");
            confirmFragment.show(getParentFragmentManager(), "confirmTextInput");
            getParentFragmentManager().setFragmentResultListener(ConfirmFragmentTextInput.requestKey, this, (requestKey, result) -> {
                String newPhoneNumber = result.getString(ConfirmFragmentTextInput.inputKey);
                db.updatePhoneNumber(accountEmail, newPhoneNumber);
                phoneView.setText(newPhoneNumber);
            });
        });

        signOut.setOnClickListener(view -> {
            ConfirmFragmentNoInput confirmFragment = ConfirmFragmentNoInput.newInstance(
                    "CONFIRM SIGNING OUT",
                    "This action will sign you out!",
                    "Cancel",
                    SignOutFragment.class);
            confirmFragment.show(getParentFragmentManager(), "confirmNoInput");
        });

        deleteAccount.setOnClickListener(view ->{
            ConfirmFragmentNoInput confirmFragment = ConfirmFragmentNoInput.newInstance(
                    "DELETE ACCOUNT",
                    "Are you sure you want to delete your account? This will log you out!",
                    "Cancel",
                    SignOutFragment.class);
            confirmFragment.show(getParentFragmentManager(), "confirmNoInput");
        });



    }

    /**
     * Shows a short-length {@link Toast} with the given message in this Fragment's context
     * @param msg message to display to the user
     */
    private void toast(String msg) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
    }
}
