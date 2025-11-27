package com.example.evently.data;

import static com.example.evently.data.generic.Promise.promise;
import static com.example.evently.data.generic.PromiseOpt.promiseOpt;

import java.util.HashMap;
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
    private final CollectionReference adminRef;
    private final CollectionReference accountsRef;

    /**
     * Sets up the collection of accounts for the AccountDB
     */
    public AccountDB() {
        // Defines the reference to the accounts collection
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        accountsRef = db.collection("accounts");
        adminRef = db.collection("admin");
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
        return storeAccount(a, null);
    }

    public Promise<Void> storeAccount(Account a, String deviceID) {
        DocumentReference docRef = accountsRef.document(a.email());
        final var obj = a.toHashMap();
        obj.put("deviceID", deviceID);
        return promise(docRef.set(obj));
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

    public PromiseOpt<Account> fetchAccountByDeviceID(String deviceID) {
        final var prom =
                promise(accountsRef.whereEqualTo("deviceID", deviceID).limit(1).get());
        return promiseOpt(prom.map(qs -> {
            if (qs.isEmpty()) {
                return Optional.empty();
            } else {
                return getAccountFromSnapshot(qs.getDocuments().get(0));
            }
        }));
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
     * Updates an accounts name in the database based on the email primary key.
     * @param email The email of the user
     * @param name The new phone number
     * @return Reference to the concurrent task.
     */
    public Promise<Void> updateName(String email, String name) {
        return updateField(email, "name", name);
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
     * The following method checks if an account is an admin account
     * @param email The email of the account being verified
     * @return A boolean about if the account is admin or not.
     */
    public Promise<Boolean> isAdmin(String email) {
        return promise(adminRef.document(email).get()).map(DocumentSnapshot::exists);
    }

    /**
     * The following method sets an account as an Admin, only to be used for testing.
     * For non-testing, admins should be created through the firebase console
     * @param email The email of the account being turned admin
     * @return A promise that the account will become an admin account.
     */
    @TestOnly
    public Promise<Void> setAdmin(String email) {

        /**
         * The following 2 lines of code are based on a response from the LLM Claude Sonnet 4.5 by
         * Anthropic: "how to store only document IDs in firebase from android with java? No data
         * needs to be stored. Only the documentID"
         *
         * According to the response, we need to add at least one field because Firestore does not
         * support empty documents.
         */
        HashMap<String, Object> emptyHashMap = new HashMap<>();
        emptyHashMap.put("exists", true);

        // Creates the document in the admin account list, saying that it exists.
        return promise(adminRef.document(email).set(emptyHashMap));
    }

    /**
     * Nuke the accounts collection and all associated data.
     */
    @TestOnly
    public Promise<Void> nuke() {
        final var accountsGetP = promise(accountsRef.get());
        final var adminGetP = promise(adminRef.get());
        return accountsGetP.with(adminGetP, (accountDocs, adminDocs) -> {
            WriteBatch batch = FirebaseFirestore.getInstance().batch();
            Stream.concat(accountDocs.getDocuments().stream(), adminDocs.getDocuments().stream())
                    .forEach(doc -> {
                        batch.delete(doc.getReference());
                    });
            return promise(batch.commit());
        });
    }
}
