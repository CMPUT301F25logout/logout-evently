package com.example.evently.ui.login;

import java.util.Objects;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.credentials.exceptions.GetCredentialCancellationException;
import androidx.credentials.exceptions.GetCredentialInterruptedException;
import androidx.credentials.exceptions.GetCredentialUnsupportedException;
import androidx.credentials.exceptions.NoCredentialException;

import com.example.evently.databinding.ActivityAuthBinding;
import com.google.android.gms.common.SignInButton;
import com.google.firebase.auth.AuthResult;

import com.example.evently.MainActivity;
import com.example.evently.R;
import com.example.evently.utils.AuthConstants;

public class AuthActivity extends AppCompatActivity {
    private boolean activityRecreated;
    private boolean hasRegisterForm = false;
    private FirebaseLogin firebaseLogin;
    private ActivityAuthBinding binding;

    private Intent transition;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAuthBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseLogin = new FirebaseLogin(this);
        activityRecreated = savedInstanceState != null;
        transition = new Intent(AuthActivity.this, MainActivity.class);

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
        Log.i("AuthActivity", "LOGGING IN!");
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
                .setFragmentResultListener("register", this, (var key, var bundle) -> {
                    // TODO (chase): The bundle should contain data to persist in the DB regarding the account.
                    startActivity(transition);
                    finish();
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
