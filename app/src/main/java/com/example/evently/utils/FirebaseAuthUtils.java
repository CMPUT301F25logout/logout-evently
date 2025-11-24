package com.example.evently.utils;

import static com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL;

import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import android.app.Activity;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.messaging.FirebaseMessaging;

import com.example.evently.BuildConfig;
import com.example.evently.data.AccountDB;
import com.example.evently.data.EventsDB;

public final class FirebaseAuthUtils {

    public static boolean testRun = false;

    /**
     * This function will throw if called before AuthActivity gets through (i.e user is logged in).
     * @return Email of the currently logged in user.
     */
    public static String getCurrentEmail() {
        var auth = FirebaseAuth.getInstance();
        return Objects.requireNonNull(auth.getCurrentUser()).getEmail();
    }

    /**
     * This function will sign out the current user instance, and delete the current token
     * @param onCompleteListener Listener to call upon completion
     */
    public static void signOut(OnCompleteListener<Void> onCompleteListener) {
        FirebaseAuth.getInstance().signOut();

        // Disable FCM auto init and remove the token so the device no longer gets notifications.
        FirebaseMessaging.getInstance().setAutoInitEnabled(false);
        FirebaseMessaging.getInstance().deleteToken().addOnCompleteListener(onCompleteListener);
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
        EventsDB eventsDB = new EventsDB();
        if (user == null)
            throw new RuntimeException(
                    "Delete account error: user is Null"); // This shouldn't happen

        if (testRun) {
            accountDB.deleteAccount(getCurrentEmail());
            user.delete().addOnSuccessListener(onSuccess).addOnFailureListener(onException::accept);
            return;
        }

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
