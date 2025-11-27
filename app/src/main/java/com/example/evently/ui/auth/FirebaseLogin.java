package com.example.evently.ui.auth;

import static com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL;

import java.util.concurrent.Executors;
import java.util.function.Consumer;

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
import androidx.credentials.exceptions.NoCredentialException;
import androidx.fragment.app.FragmentActivity;

import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

import com.example.evently.BuildConfig;

/**
 * Utility class for handling sign in/register with google using Firebase auth.
 */
class FirebaseLogin {
    // Context under which the auth may be used.
    private final FragmentActivity act;
    private final FirebaseAuth mAuth;
    private final CredentialManager credentialManager;

    /**
     * @param act The activity context under which authentication flow is being used.
     * @apiNote Take care to keep the activity context up to date. If the activity is recreated, this object
     *   must be recreated too.
     */
    protected FirebaseLogin(FragmentActivity act) {
        this.act = act;
        this.mAuth = FirebaseAuth.getInstance();
        this.credentialManager = CredentialManager.create(act.getBaseContext());
    }

    /**
     * @return Whether local storage contains a session ID already (i.e user can be logged back in automatically)
     * @apiNote It is expected that other activities use FirebaseAuthUtils::getCurrentEmail when needed to get the
     *  logged in user. This way, the user doesn't need to be passed around everywhere and saved to local storage
     *  manually. Firebase already does that.
     */
    protected boolean isLoggedIn() {
        return mAuth.getCurrentUser() != null;
    }

    /**
     * Launch the login flow and thus take the user to the "Google sign in" dialog (part of the Android platform).
     * @param newUser Whether or not this is for a completely new user (i.e registration).
     * @param onSuccess UI thread callback for successful login/registration.
     *                  This will have access to the {@link AuthResult}, which contains information on the logged in user.
     * @param onGoogleIdFailure UI thread callback in case google sign in fails.
     *                          If newUser is set to false but sign in was indeed tried with a brand new user,
     *                          a {@link NoCredentialException} may be raised.
     * @param onException UI thread callback in case of catastrophic, probably unrecoverable exceptions.
     */
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

        if (!(credential instanceof CustomCredential customCredential)
                || !credential
                        .getType()
                        .equals(TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)) { // This shouldn't happen.
            onException.accept(new Exception("absurd: Credential was not a Google ID token"));
            return;
        }

        // Create Google ID Token
        Bundle credentialData = customCredential.getData();
        var googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credentialData);

        // Sign in to Firebase with using the token
        var firebaseCred =
                GoogleAuthProvider.getCredential(googleIdTokenCredential.getIdToken(), null);
        mAuth.signInWithCredential(firebaseCred).addOnCompleteListener(act, task -> {
            if (task.isSuccessful()) {
                // Sign in success, delegate to callback.
                act.runOnUiThread(() -> onSuccess.accept(task.getResult()));
            } else {
                // Exceptional scenario where firebase auth fails.
                act.runOnUiThread(() -> onException.accept(task.getException()));
            }
        });
    }
}
