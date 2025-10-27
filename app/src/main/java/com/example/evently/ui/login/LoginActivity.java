package com.example.evently.ui.login;

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

import com.example.evently.MainActivity;
import com.example.evently.R;
import com.google.android.gms.common.SignInButton;
import com.google.firebase.auth.AuthResult;

import java.util.Objects;
import java.util.function.Consumer;

public class LoginActivity extends AppCompatActivity {
    static final int MAX_RETRY_LOGIN = 5;
    private boolean activityRecreated;
    private FirebaseLogin firebaseLogin;
    private SignInButton manualLoginBtn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseLogin = new FirebaseLogin(this);
        activityRecreated = savedInstanceState != null;
        manualLoginBtn = findViewById(R.id.login);
    }

    @Override
    public void onStart() {
        super.onStart();


        if (activityRecreated) {
            // If it was recreated, auto login has already been tried and the fragment already exists.
            // Nothing to be done.
            return;
        }

        // Essentially a panic meant for very exceptional circumstance.
        Consumer<Exception> onException = e -> {
            Log.e("LoginActivity", Objects.requireNonNullElse(e.getLocalizedMessage(), e.toString()));
            Toast.makeText(this, "Something went wrong...", Toast.LENGTH_SHORT).show();
        };

        Consumer<AuthResult> successfulLogin = res -> {
            // Log in successful.
            new Intent(LoginActivity.this, MainActivity.class);
        };

        if (firebaseLogin.isLoggedIn()) {
            // Already signed in - move on to next activity
            new Intent(LoginActivity.this, MainActivity.class);
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
                            if (retryCount > MAX_RETRY_LOGIN) {
                                unrecoverableError(ie);
                                return;
                            }
                            tryLoggingIn(retryCount + 1);
                        }
                        case GetCredentialUnsupportedException ue ->
                            // This device does not support credential manager.
                            // Our app simply cannot work on this device.
                            // TODO (chase): Might be worth showing an alert dialog here.
                            Toast.makeText(this, "Device unsupported; Sorry!", Toast.LENGTH_SHORT).show();
                        case NoCredentialException ne -> {
                            // This is likely a totally new user and must register first.
                            // Hand off to the register fragment.
                            manualLoginBtn.setVisibility(View.INVISIBLE);
                            getSupportFragmentManager().beginTransaction()
                                    .setReorderingAllowed(true)
                                    .add(R.id.register_form_container, RegisterFragment.class, null)
                                    .commit();
                        }
                        default ->
                            Log.e("LoginActivity.GetCredentialCustomException", Objects.requireNonNullElse(e.getLocalizedMessage(), e.toString()));
                    }
                },
                this::unrecoverableError
        );
    }

    private void successfulLogin(AuthResult res) {
        new Intent(LoginActivity.this, MainActivity.class);
    }

    private void unrecoverableError(Exception e) {
        Log.e("LoginActivity.unrecoverableError", Objects.requireNonNullElse(e.getLocalizedMessage(), e.toString()));
        Toast.makeText(this, "Something went catastrophically wrong...", Toast.LENGTH_SHORT).show();
    }
}
