package com.example.evently.ui.auth;

import java.util.Objects;
import java.util.Optional;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.credentials.exceptions.GetCredentialCancellationException;
import androidx.credentials.exceptions.GetCredentialInterruptedException;
import androidx.credentials.exceptions.GetCredentialUnsupportedException;
import androidx.credentials.exceptions.NoCredentialException;

import com.example.evently.data.AccountDB;
import com.example.evently.data.model.Account;
import com.google.firebase.auth.AuthResult;

import com.example.evently.R;
import com.example.evently.databinding.ActivityAuthBinding;
import com.example.evently.ui.entrant.EntrantActivity;
import com.example.evently.utils.AuthConstants;

/**
 * The overarching activity for managing authentication. This is the activity launched
 * at the start. If a user session exists (i.e user has logged in recently), it will immediately
 * transition to the next activity.
 * <p>
 * Otherwise, it'll try to prompt the user for sign in (if they have signed in once before).
 * <p>
 * If it's a totally new user, it will show the registration form via {@link RegisterFragment}.
 * @see RegisterFragment
 * @see FirebaseLogin
 */
public class AuthActivity extends AppCompatActivity {
    // Whether or not the activity was _re-created_.
    private boolean activityRecreated;
    /**
     * Whether or not the activity already has a registration fragment.
     * We don't want to add fragment on top of fragment. onStart may be called several times
     *   without the activity being recreated.
     */
    private boolean hasRegisterForm = false;

    private FirebaseLogin firebaseLogin;
    private ActivityAuthBinding binding;

    // Utility for transitioning to the next activity.
    private Intent transition;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAuthBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseLogin = new FirebaseLogin(this);
        activityRecreated = savedInstanceState != null;
        transition = new Intent(AuthActivity.this, EntrantActivity.class);

        // Manual buttons in case user refuses the auto login prompt.
        binding.login.setOnClickListener(v -> tryLoggingIn(0));
        binding.registerForm.setOnClickListener(v -> showRegisterForm());
    }

    @Override
    public void onStart() {
        super.onStart();

        if (activityRecreated || hasRegisterForm) {
            // If it was recreated/resumed, auto login has already been tried
            // and the fragment already exists.
            // Nothing to be done.
            return;
        }

        if (firebaseLogin.isLoggedIn()) {
            // Already signed in - move on to next activity
            startActivity(transition);
            finish();
            return;
        }

        // Otherwise, try logging in (not register).
        tryLoggingIn(0);
    }

    private void showRegisterForm() {
        binding.login.setVisibility(View.INVISIBLE);
        binding.registerForm.setVisibility(View.INVISIBLE);
        getSupportFragmentManager()
                .beginTransaction()
                .setReorderingAllowed(true)
                .setCustomAnimations(R.animator.fade_in, R.animator.fade_out)
                .add(R.id.register_form_container, RegisterFragment.class, null)
                .commit();
        getSupportFragmentManager()
                .setFragmentResultListener(
                        RegisterFragment.resultKey, this, (var key, var bundle) -> {

                            String email = bundle.getString("email");
                            String name = bundle.getString("name");
                            String phone = bundle.getString("phone");

                            // Stores a new account
                            AccountDB accountDB = new AccountDB();
                            accountDB.fetchAccount(
                                    email,
                                    optionalAccount -> {
                                        if (optionalAccount.isEmpty()) {
                                            // If user is not found, we need to create an account
                                            // for them with the new information
                                            Account newAccount = new Account(
                                                    email, name, Optional.ofNullable(phone), email);
                                            accountDB.storeAccount(newAccount);
                                            startActivity(transition);
                                            finish();
                                        } else {
                                            // NOTE: If the bundle contains an email that already
                                            // exists in the DB, show an error and prompt the user
                                            // to login instead of registering.
                                            Toast.makeText(this, "Account already registered: Please login", Toast.LENGTH_SHORT)
                                                    .show();

                                            /**
                                             * The following line of code is from Stack Overflow:
                                             * Title: "how to remove view inside fragment"
                                             * Author: "Jackspicer"
                                             * Response: https://stackoverflow.com/a/25122894
                                             * Response Author: "Martin"
                                             */
                                            findViewById(R.id.register_form_container).setVisibility(View.GONE);
                                            binding.login.setVisibility(View.VISIBLE);

                                        }
                                    },
                                    e -> {});


                        });
        hasRegisterForm = true;
    }

    private void tryLoggingIn(int retryCount) {
        firebaseLogin.launchLogin(
                false,
                this::successfulLogin,
                e -> {
                    switch (e) {
                        case GetCredentialCancellationException ce -> {
                            // The user cancelled the auto sign in request...
                            // Expose buttons to manually sign in or register.
                            binding.login.setVisibility(View.VISIBLE);
                            binding.registerForm.setVisibility(View.VISIBLE);
                        }
                        case GetCredentialInterruptedException ie -> {
                            // Retry (unless we retried too many times already).
                            if (retryCount > AuthConstants.MAX_RETRY) {
                                unrecoverableError(ie);
                                return;
                            }
                            tryLoggingIn(retryCount + 1);
                        }
                        case GetCredentialUnsupportedException ue ->
                            // This device does not support credential manager.
                            // Our app simply cannot work on this device.
                            // TODO (chase): Might be worth showing an alert dialog here.
                            Toast.makeText(this, "Device unsupported; Sorry!", Toast.LENGTH_SHORT)
                                    .show();
                        case NoCredentialException ne -> {
                            // This is likely a totally new user and must register first.
                            // Hand off to the register fragment.
                            showRegisterForm();
                        }
                        default ->
                            Log.e(
                                    "AuthActivity.GetCredentialCustomException",
                                    Objects.requireNonNullElse(
                                            e.getLocalizedMessage(), e.toString()));
                    }
                },
                this::unrecoverableError);
    }

    private void successfulLogin(AuthResult res) {
        startActivity(transition);
        finish();
    }

    private void unrecoverableError(Exception e) {
        Log.e(
                "LoginActivity.unrecoverableError",
                Objects.requireNonNullElse(e.getLocalizedMessage(), e.toString()));
        Toast.makeText(this, "Something went catastrophically wrong...", Toast.LENGTH_SHORT)
                .show();
    }
}
