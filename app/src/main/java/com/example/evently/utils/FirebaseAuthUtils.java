package com.example.evently.utils;

import static com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL;

import android.app.Activity;
import android.os.CancellationSignal;

import androidx.annotation.NonNull;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;

import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import com.example.evently.BuildConfig;
import com.example.evently.data.AccountDB;
import com.example.evently.data.EventsDB;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.messaging.FirebaseMessaging;

public final class FirebaseAuthUtils {

    /**
     * This function will throw if called before AuthActivity gets through (i.e user is logged in).
     * @return Email of the currently logged in user.
     */
    public static String getCurrentEmail() {
        var auth = FirebaseAuth.getInstance();
        return Objects.requireNonNull(auth.getCurrentUser()).getEmail();
    }

    public static void signOut(OnCompleteListener<Void> onCompleteListener) {
        FirebaseAuth.getInstance().signOut();

        // Disable FCM auto init and remove the token so the device no longer gets notifications.
        FirebaseMessaging.getInstance().setAutoInitEnabled(false);
        FirebaseMessaging.getInstance().deleteToken().addOnCompleteListener(onCompleteListener);
    }

    public static void deleteAccount(Activity activity, OnSuccessListener<Void> onSuccess, Consumer<Exception> onException) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        AccountDB accountDB = new AccountDB();
        EventsDB eventsDB = new EventsDB();
        if (user == null) throw new RuntimeException("Delete account error: user is Null"); //This shouldn't happen

        CredentialManager credentialManager = CredentialManager.create(activity);
        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setServerClientId(BuildConfig.GOOGLE_CLIENT_ID)
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        credentialManager.getCredentialAsync(
                activity.getBaseContext(),
                request,
                new CancellationSignal(),
                Executors.newSingleThreadExecutor(),
                new CredentialManagerCallback<>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        Credential credential = result.getCredential();
                        if (!credential.getType().equals(TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)) {
                            // This shouldn't happen.
                            onException.accept(
                                    new Exception("absurd: Credential was not a Google ID token")
                            );
                            return;
                        }
                        user.reauthenticate(
                                GoogleAuthProvider.getCredential(
                                        GoogleIdTokenCredential.createFrom(credential.getData()).getIdToken(),
                                        null))
                                .addOnSuccessListener(task -> {
                                    eventsDB.removeUserFromEvents(getCurrentEmail());
                                    accountDB.deleteAccount(getCurrentEmail());
                                    user.delete().addOnSuccessListener(onSuccess);
                                })
                                .addOnFailureListener(onException::accept);

                    }

                    @Override
                    public void onError(@NonNull GetCredentialException e) {
                        onException.accept(e);
                    }
                });


    }


    private FirebaseAuthUtils() {}
}
