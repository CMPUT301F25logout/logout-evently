package com.example.evently.utils;

import static com.example.evently.data.generic.Promise.promise;
import static com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL;

import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import android.app.Activity;
import android.content.Context;
import android.os.CancellationSignal;
import android.provider.Settings;
import android.util.Pair;
import androidx.annotation.NonNull;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import com.example.evently.BuildConfig;
import com.example.evently.data.AccountDB;
import com.example.evently.data.generic.Promise;

public final class FirebaseAuthUtils {

    /**
     * @return Whether or not there is a user currently logged in.
     */
    public static boolean isLoggedIn() {
        return FirebaseAuth.getInstance().getCurrentUser() != null;
    }

    /**
     * This function will throw if called before AuthActivity gets through (i.e user is logged in).
     * @return Email of the currently logged in user.
     */
    public static String getCurrentEmail() {
        var auth = FirebaseAuth.getInstance();
        return Objects.requireNonNull(auth.getCurrentUser()).getEmail();
    }

    /**
     * Make the firebase auth instance log in using device ID.
     * @return Promise of logging in.
     */
    public static Promise<AuthResult> dumbLogin(Context ctx) {
        final var auth = FirebaseAuth.getInstance();
        final var accountDB = new AccountDB();
        final var deviceID = getDeviceID(ctx);
        return accountDB.fetchAccountByDeviceID(deviceID).then(accOpt -> {
            if (accOpt.isEmpty()) {
                throw new IllegalArgumentException("No such account");
            }
            return promise(auth.signInWithEmailAndPassword(accOpt.get().email(), deviceID));
        });
    }

    /**
     * Make the firebase auth instance sign up using email device ID.
     * @return Promise of signing up.
     */
    public static Promise<Pair<AuthResult, String>> dumbSignUp(Context ctx, String email) {
        final var auth = FirebaseAuth.getInstance();
        final var deviceID = getDeviceID(ctx);

        return promise(auth.createUserWithEmailAndPassword(email, deviceID))
                .with(Promise.of(deviceID));
    }

    /**
     * Check whether or not we're currently logged in as a "device ID" identified account.
     * @return True if logged in with device ID.
     */
    public static boolean isDumbAccount() {
        var auth = FirebaseAuth.getInstance();
        // Assumption: Each account is linked to only one provider.
        return Objects.requireNonNull(auth.getCurrentUser())
                .getProviderData()
                .get(0)
                .getProviderId()
                .equals(EmailAuthProvider.PROVIDER_ID);
    }

    public static String getDeviceID(Context ctx) {
        return Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    /**
     * This function will sign out the current user instance, and delete the current token
     */
    public static void signOut() {
        FirebaseAuth.getInstance().signOut();
    }

    /**
     * This function will sign out the current user and delete their data in the database. The user
     * wil be required to reauthenticate
     * @param activity activity deleteAccount is called from
     * @param onSuccess listener to be called on success
     * @param onException consumer accepting exceptions
     */
    public static void deleteAccount(
            Activity activity, OnSuccessListener<Void> onSuccess, Consumer<Exception> onException) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        AccountDB accountDB = new AccountDB();
        if (user == null)
            throw new RuntimeException(
                    "Delete account error: user is Null"); // This shouldn't happen

        requestGoogleCredential(
                activity,
                token -> user.reauthenticate(GoogleAuthProvider.getCredential(token, null))
                        .addOnSuccessListener(task -> {
                            accountDB.deleteAccount(getCurrentEmail());
                            user.delete()
                                    .addOnSuccessListener(onSuccess)
                                    .addOnFailureListener(onException::accept);
                        })
                        .addOnFailureListener(onException::accept),
                onException);
    }

    /**
     * Gets google credential token from CredentialManager
     * @param activity Activity from which the context of the request is made
     * @param onToken Callback to be performed on token retrieved successfully
     * @param onException Callback to be performed on exception
     */
    private static void requestGoogleCredential(
            Activity activity, Consumer<String> onToken, Consumer<Exception> onException) {
        CredentialManager credentialManager = CredentialManager.create(activity);

        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setServerClientId(BuildConfig.GOOGLE_CLIENT_ID)
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        credentialManager.getCredentialAsync(
                activity,
                request,
                new CancellationSignal(),
                Executors.newSingleThreadExecutor(),
                new CredentialManagerCallback<>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        try {
                            Credential credential = result.getCredential();
                            if (!credential.getType().equals(TYPE_GOOGLE_ID_TOKEN_CREDENTIAL))
                                throw new Exception(
                                        "absurd: Credential was not a Google ID token"); // This
                            // shouldn't happen.
                            String token = GoogleIdTokenCredential.createFrom(credential.getData())
                                    .getIdToken();
                            onToken.accept(token);
                        } catch (Exception e) {
                            onException.accept(e);
                        }
                    }

                    @Override
                    public void onError(@NonNull GetCredentialException e) {
                        onException.accept(e);
                    }
                });
    }

    private FirebaseAuthUtils() {}
}
