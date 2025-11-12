package com.example.evently.data;

import static com.example.evently.data.generic.Promise.promise;
import static com.example.evently.data.generic.PromiseOpt.promiseOpt;

import java.util.Optional;
import java.util.stream.Stream;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import org.jetbrains.annotations.TestOnly;

import com.example.evently.data.generic.Promise;
import com.example.evently.data.generic.PromiseOpt;
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
     * @return Reference to the concurrent task.
     */
    public Promise<Void> storeAccount(Account a) {
        DocumentReference docRef = accountsRef.document(a.email());
        return promise(docRef.set(a.toHashMap()));
    }

    /**
     * Returns an account based based on an email. Also takes in onSuccess, and onFailure listeners.
     * @param email The email of the target account
     * @return Reference to the concurrent task yielding to an account (if found).
     */
    public PromiseOpt<Account> fetchAccount(String email) {
        return promiseOpt(
                promise(accountsRef.document(email).get()).map(AccountDB::getAccountFromSnapshot));
    }

    /**
     * Delete an account from the database by email
     * @param email The email of the target account
     * @return Reference to the concurrent task.
     */
    public Promise<Void> deleteAccount(String email) {
        return promise(accountsRef.document(email).delete());
    }

    /**
     * Updates an accounts phone number in the database based on the email primary key.
     * @param email The email of the user
     * @param phoneNumber The new phone number
     * @return Reference to the concurrent task.
     */
    public Promise<Void> updatePhoneNumber(String email, String phoneNumber) {
        return updateField(email, "phoneNumber", phoneNumber);
    }

    /**
     * Updates an accounts visible email in the database. Has a callback for onSuccess, and onException
     * @param primaryEmail The original email of the user for login.
     * @param newVisibleEmail The new visible email
     * @return Reference to the concurrent task.
     */
    public Promise<Void> updateVisibleEmail(String primaryEmail, String newVisibleEmail) {
        return updateField(primaryEmail, "visibleEmail", newVisibleEmail);
    }

    // Helper for updating any field for an account.
    private Promise<Void> updateField(String email, String field, Object newValue) {
        // Gets an account based on the email
        DocumentReference docRef = accountsRef.document(email);
        return promise(docRef.update(field, newValue));
    }

    /**
     * Nuke the accounts collection and all associated data.
     */
    @TestOnly
    public Promise<Void> nuke() {
        return promise(accountsRef.get()).then(docs -> {
            WriteBatch batch = FirebaseFirestore.getInstance().batch();
            Stream.concat(docs.getDocuments().stream(), docs.getDocuments().stream())
                    .forEach(doc -> {
                        batch.delete(doc.getReference());
                    });
            return promise(batch.commit());
        });
    }
}
