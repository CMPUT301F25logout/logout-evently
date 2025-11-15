package com.example.evently.ui.common;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
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

import com.example.evently.MainActivity;
import com.example.evently.R;
import com.example.evently.data.AccountDB;
import com.example.evently.data.model.Account;
import com.example.evently.data.model.Category;
import com.example.evently.data.model.Event;
import com.example.evently.ui.auth.AuthActivity;
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

import kotlin.text.UStringsKt;

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

            account.phoneNumber().ifPresentOrElse(n -> phoneView.setText(formatPhoneNumber(n)), () -> phoneView.setText("None"));
            headerView.setText(String.format("%s's Profile", account.name()));
        });

        editEmail.setOnClickListener(view -> {
            ConfirmFragmentTextInput confirmFragment = ConfirmFragmentTextInput.newInstance(
                    "Change Email",
                    "Please enter your new email below.",
                    "New Email",
                    s -> Patterns.EMAIL_ADDRESS.matcher(s).matches());
            confirmFragment.show(getParentFragmentManager(), "confirmTextInput");

            getParentFragmentManager().setFragmentResultListener(
                    ConfirmFragmentTextInput.requestKey,
                    this,
                    (requestKey, result) -> {
                String newEmail = result.getString(ConfirmFragmentTextInput.inputKey);
                db.updateVisibleEmail(accountEmail, newEmail);
                emailView.setText(newEmail);
            });
        });

        editPhone.setOnClickListener(view -> {
            ConfirmFragmentTextInput confirmFragment = ConfirmFragmentTextInput.newInstance(
                    "Change Phone Number",
                    "Please enter your new phone number below.",
                    "(000) 000-0000",
                    s -> Patterns.PHONE.matcher(s).matches());
            confirmFragment.show(getParentFragmentManager(), "confirmTextInput");

            getParentFragmentManager().setFragmentResultListener(
                    ConfirmFragmentTextInput.requestKey,
                    this,
                    (requestKey, result) -> {
                String number = result.getString(ConfirmFragmentTextInput.inputKey);
                if (number != null) number = formatPhoneNumber(number);
                db.updatePhoneNumber(accountEmail, number);
                phoneView.setText(number);
            });
        });

        signOut.setOnClickListener(view -> {
            ConfirmFragmentNoInput confirmFragment = ConfirmFragmentNoInput.newInstance(
                    "CONFIRM SIGNING OUT",
                    "This action will sign you out!");
            confirmFragment.show(getParentFragmentManager(), "confirmNoInput");
            getParentFragmentManager().setFragmentResultListener(
                    ConfirmFragmentNoInput.requestKey,
                    this,
                    (requestKey, result) -> {
                if (!result.getBoolean(ConfirmFragmentNoInput.inputKey)) return;
                FirebaseAuthUtils.signOut(task -> {
                    if (task.isSuccessful()) {
                        Intent intent = new Intent(getActivity(), AuthActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        return;
                    }
                    Log.w("EditProfileFragment", "Unable to log out: ", task.getException());
                    Toast.makeText(
                                    requireContext(),
                                    "Unable to sign out",
                                    Toast.LENGTH_SHORT)
                            .show();
                });
            });
        });

        deleteAccount.setOnClickListener(view ->{
            ConfirmFragmentNoInput confirmFragment = ConfirmFragmentNoInput.newInstance(
                    "DELETE ACCOUNT",
                    "Are you sure you want to delete your account? This will log you out!");
            confirmFragment.show(getParentFragmentManager(), "confirmNoInput");
            getParentFragmentManager().setFragmentResultListener(
                    ConfirmFragmentNoInput.requestKey,
                    this,
                    (requestKey, result) -> {
                        if (!result.getBoolean(ConfirmFragmentNoInput.inputKey)) return;
                        FirebaseAuthUtils.deleteAccount(getActivity(),
                                task -> {
                                    Intent intent = new Intent(getActivity(), AuthActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                },
                                e -> {
                                    Log.w("EditProfileFragment", "Unable to delete account: ", e);
                                    Toast.makeText(requireContext(),
                                                    "Unable to delete account",
                                                    Toast.LENGTH_SHORT)
                                            .show();
                                });
                    });
        });
    }

    private String formatPhoneNumber(String unformattedNumber) {
        if (!Patterns.PHONE.matcher(unformattedNumber).matches()) return "None";
        String phoneNum = unformattedNumber.replaceAll("\\D", "");
        return String.format("(%s) %s-%s",
                phoneNum.substring(0, 3),
                phoneNum.substring(3, 6),
                phoneNum.substring(6, 10));
    }

}
