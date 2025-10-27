package com.example.evently.ui.login;

import java.util.Objects;

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

import com.google.android.gms.common.SignInButton;
import com.google.firebase.auth.AuthResult;

import com.example.evently.MainActivity;
import com.example.evently.R;
import com.example.evently.utils.AuthConstants;

public class AuthActivity extends AppCompatActivity {
    private boolean activityRecreated;
    private FirebaseLogin firebaseLogin;
    private SignInButton manualLoginBtn;

    private Intent transition;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        firebaseLogin = new FirebaseLogin(this);
        activityRecreated = savedInstanceState != null;
        manualLoginBtn = findViewById(R.id.login);
        transition = new Intent(AuthActivity.this, MainActivity.class);

        manualLoginBtn.setOnClickListener(v -> tryLoggingIn(0));
    }

    @Override
    public void onStart() {
        super.onStart();

        if (activityRecreated) {
            // If it was recreated, auto login has already been tried and the fragment already
            // exists.
            // Nothing to be done.
            return;
        }

        if (firebaseLogin.isLoggedIn()) {
            // Already signed in - move on to next activity
            startActivity(transition);
            return;
        }

        // Otherwise, try logging in (not register).
        tryLoggingIn(0);
    }

    private void tryLoggingIn(int retryCount) {
        firebaseLogin.launchLogin(
                false,
                this::successfulLogin,
                e -> {
                    switch (e) {
                        case GetCredentialCancellationException ce ->
                            // The user cancelled the sign in request...
                            // Let them try again by exposing a button that exposes the same flow.
                            manualLoginBtn.setVisibility(View.VISIBLE);
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
                            manualLoginBtn.setVisibility(View.INVISIBLE);
                            getSupportFragmentManager()
                                    .beginTransaction()
                                    .setReorderingAllowed(true)
                                    .add(R.id.register_form_container, RegisterFragment.class, null)
                                    .commit();
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
    }

    private void unrecoverableError(Exception e) {
        Log.e(
                "LoginActivity.unrecoverableError",
                Objects.requireNonNullElse(e.getLocalizedMessage(), e.toString()));
        Toast.makeText(this, "Something went catastrophically wrong...", Toast.LENGTH_SHORT)
                .show();
    }
}
