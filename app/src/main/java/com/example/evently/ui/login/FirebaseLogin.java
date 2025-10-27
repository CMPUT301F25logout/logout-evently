package com.example.evently.ui.login;

import static com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL;

import java.util.concurrent.Executors;
import java.util.function.Consumer;

import android.app.Activity;
import android.os.Bundle;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;

import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

import com.example.evently.BuildConfig;

class FirebaseLogin {
    private final Activity act;
    private final FirebaseAuth mAuth;
    private final CredentialManager credentialManager;

    protected FirebaseLogin(Activity act) {
        this.act = act;
        this.mAuth = FirebaseAuth.getInstance();
        this.credentialManager = CredentialManager.create(act.getBaseContext());
    }

    /**
     * @return Whether local storage contains a session ID already (i.e user can be logged back in automatically)
     */
    protected boolean isLoggedIn() {
        return mAuth.getCurrentUser() != null;
    }

    protected void launchLogin(
            boolean newUser,
            Consumer<AuthResult> onSuccess,
            Consumer<GetCredentialException> onGoogleIdFailure,
            Consumer<Exception> onException) {
        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(!newUser)
                .setServerClientId(BuildConfig.GOOGLE_CLIENT_ID)
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        credentialManager.getCredentialAsync(
                act.getBaseContext(),
                request,
                new CancellationSignal(),
                Executors.newSingleThreadExecutor(),
                new CredentialManagerCallback<>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        // Extract credential and sign in with it.
                        handleSignIn(result.getCredential(), onSuccess, onException);
                    }

                    @Override
                    public void onError(@NonNull GetCredentialException e) {
                        // This probably indicates the user has never registered with the app
                        // if setFilterByAuthorizedAccounts was set to true.
                        act.runOnUiThread(() -> onGoogleIdFailure.accept(e));
                    }
                });
    }

    private void handleSignIn(
            Credential credential,
            Consumer<AuthResult> onSuccess,
            Consumer<Exception> onException) {
        if (credential instanceof CustomCredential customCredential
                && credential.getType().equals(TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)) {
            // Create Google ID Token
            Bundle credentialData = customCredential.getData();
            GoogleIdTokenCredential googleIdTokenCredential =
                    GoogleIdTokenCredential.createFrom(credentialData);

            // Sign in to Firebase with using the token
            AuthCredential firebaseCred =
                    GoogleAuthProvider.getCredential(googleIdTokenCredential.getIdToken(), null);
            mAuth.signInWithCredential(firebaseCred).addOnCompleteListener(act, task -> {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    act.runOnUiThread(() -> onSuccess.accept(task.getResult()));
                } else {
                    // Exceptional scenario where firebase auth fails.
                    act.runOnUiThread(() -> onException.accept(task.getException()));
                }
            });
        } else {
            // This _shouldn't_ happen.
            onException.accept(new Exception("absurd: Credential was not a Google ID token"));
        }
    }
}
