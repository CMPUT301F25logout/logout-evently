package com.example.evently.data;

import java.util.Optional;
import java.util.function.Consumer;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import com.example.evently.data.model.Account;

/**
 * The account database for fetching, storing, deleting, and updating accounts in the database
 * @author alexander-b
 */
public class AccountDB {

    // Reference to the accounts collection
    private final CollectionReference accountsRef;

    /**
     * Sets up the collection of accounts for the AccountDB
     */
    public AccountDB() {
        // Defines the reference to the accounts collection
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        accountsRef = db.collection("accounts");
    }

    /**
     * Returns an account from a document snapshot.
     * @param documentSnapshot The snapshot of the account
     * @return the fetched account, if found
     */
    private static Optional<Account> getAccountFromSnapshot(DocumentSnapshot documentSnapshot)
            throws NullPointerException {

        if (!documentSnapshot.exists()) return Optional.empty();

        String stringPhoneNum = documentSnapshot.getString("phoneNumber");
        Optional<String> optionalPhoneNum = Optional.ofNullable(stringPhoneNum);

        // Creates and stores the account.
        return Optional.of(new Account(
                documentSnapshot.getId(),
                documentSnapshot.getString("name"),
                optionalPhoneNum,
                documentSnapshot.getString("visibleEmail")));
    }

    /**
     * Stores an account in the database.
     * @param a The account stored in the database.
     */
    public void storeAccount(Account a) {
        DocumentReference docRef = accountsRef.document(a.email());
        docRef.set(a.toHashMap());
    }

    /**
     * Stores an account in the database.
     * @param a The account stored in the database.
     * @param onSuccess A callback for the onSuccessListener
     * @param onException A callback for the onFailureListener
     */
    public void storeAccount(Account a, Consumer<Void> onSuccess, Consumer<Exception> onException) {
        DocumentReference docRef = accountsRef.document(a.email());
        docRef.set(a.toHashMap())
                .addOnSuccessListener(onSuccess::accept)
                .addOnFailureListener(onException::accept);
    }

    /**
     * Returns an account based based on an email. Also takes in onSuccess, and onFailure listeners.
     * @param email The email of the target account
     * @param onSuccess A callback for the onSuccessListener
     * @param onException A callback for the onFailureListener
     */
    public void fetchAccount(
            String email, Consumer<Optional<Account>> onSuccess, Consumer<Exception> onException) {

        // The following part of the code is from Anthropic, Claude Sonnet 4.5, prompt: "I have a
        // Consumer<DocumentSnapshot> callback in a function which is used when fetching a document
        // from my DB, but I want to have it as a Consumer<Account>, by mapping my documentSnapshot
        // to an account using my accountFromSnapshot function. How to do this?"
        // Code from Claude which I used:
        // "docSnapshot -> accountConsumer.accept(accountFromSnapshot(docSnapshot)"
        accountsRef
                .document(email)
                .get()
                .addOnSuccessListener(
                        docSnapshot -> onSuccess.accept(getAccountFromSnapshot(docSnapshot)))
                .addOnFailureListener(onException::accept);
    }

    /**
     * Delete an account from the database by email
     * @param email The email of the target account
     */
    public void deleteAccount(String email) {

        // The following code is from the firebase documentation on deleting documents:
        // https://firebase.google.com/docs/firestore/manage-data/delete-data
        accountsRef.document(email).delete();
    }

    /**
     * Delete an account from the database by email
     * @param email The email of the target account
     * @param onSuccess A callback for the onSuccessListener
     * @param onException A callback for the onFailureListener
     */
    public void deleteAccount(
            String email, Consumer<Void> onSuccess, Consumer<Exception> onException) {

        // The following code is from the firebase documentation on deleting documents:
        // https://firebase.google.com/docs/firestore/manage-data/delete-data
        accountsRef
                .document(email)
                .delete()
                .addOnSuccessListener(onSuccess::accept)
                .addOnFailureListener(onException::accept);
    }

    /**
     * Updates an accounts phone number in the database based on the email primary key.
     * @param email The email of the user
     * @param phoneNumber The new phone number
     */
    public void updatePhoneNumber(String email, String phoneNumber) {

        // Gets an account based on the email
        DocumentReference docRef = accountsRef.document(email);

        // The following code is from the firebase docs on how to update a field in the DB:
        // https://firebase.google.com/docs/firestore/manage-data/add-data#update-data
        docRef.update("phoneNumber", phoneNumber);
    }

    /**
     * Updates an accounts phone number in the database based on the email primary key.
     * Has a callback for onSuccess, and onException
     * @param email The email of the user
     * @param phoneNumber The new phone number
     * @param onSuccess A callback for the onSuccessListener
     * @param onException A callback for the onFailureListener
     */
    public void updatePhoneNumber(
            String email,
            String phoneNumber,
            Consumer<Void> onSuccess,
            Consumer<Exception> onException) {

        // Gets an account based on the email
        DocumentReference docRef = accountsRef.document(email);

        // The following code is from the firebase docs on how to update a field in the DB:
        // https://firebase.google.com/docs/firestore/manage-data/add-data#update-data
        docRef.update("phoneNumber", phoneNumber)
                .addOnSuccessListener(onSuccess::accept)
                .addOnFailureListener(onException::accept);
    }

    /**
     * Updates an accounts visible email in the database.
     * @param primaryEmail The original email of the user for login.
     * @param newVisibleEmail The new visible email
     */
    public void updateVisibleEmail(String primaryEmail, String newVisibleEmail) {

        // Gets an account based on the email
        DocumentReference docRef = accountsRef.document(primaryEmail);

        // The following code is from the firebase docs on how to update a field in the DB:
        // https://firebase.google.com/docs/firestore/manage-data/add-data#update-data
        docRef.update("visibleEmail", newVisibleEmail);
    }

    /**
     * Updates an accounts visible email in the database. Has a callback for onSuccess, and onException
     * @param primaryEmail The original email of the user for login.
     * @param newVisibleEmail The new visible email
     * @param onSuccess A callback for the onSuccessListener
     * @param onException A callback for the onFailureListener
     */
    public void updateVisibleEmail(
            String primaryEmail,
            String newVisibleEmail,
            Consumer<Void> onSuccess,
            Consumer<Exception> onException) {

        // Gets an account based on the email
        DocumentReference docRef = accountsRef.document(primaryEmail);

        // The following code is from the firebase docs on how to update a field in the DB:
        // https://firebase.google.com/docs/firestore/manage-data/add-data#update-data
        docRef.update("visibleEmail", newVisibleEmail)
                .addOnSuccessListener(onSuccess::accept)
                .addOnFailureListener(onException::accept);
    }
}
