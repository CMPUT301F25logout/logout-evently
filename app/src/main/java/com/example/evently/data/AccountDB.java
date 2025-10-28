package com.example.evently.data;

import com.example.evently.data.model.Account;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.function.Consumer;

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
     * Stores an account in the database
     * @param a The account stored in the database.
     */
    public void storeAccount(Account a, Consumer<Void> onSuccess, Consumer<Exception> onException) {
        DocumentReference docRef = accountsRef.document(a.email());

        // An Optional<String> cannot be stored in the DB.
        String storable_phone_num = a.phoneNumber().orElse(null);

        // Returns the task of storing an account.
        docRef.set(a.toHashMap())
                .addOnSuccessListener(onSuccess::accept)
                .addOnFailureListener(onException::accept);
    }

    /**
     * Returns an account based based on an email
     * @param email The email of the target account
     */
    public void fetchAccount(
            String email, Consumer<DocumentSnapshot> onSuccess, Consumer<Exception> onException) {

        // Returns a document snapshot with the attached account.
        accountsRef
                .document(email)
                .get()
                .addOnSuccessListener(onSuccess::accept)
                .addOnFailureListener(onException::accept);
    }

    /**
     * Delete an account from the database by email
     * @param email The email of the target account
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
     * Updates an account phone number in the database based on account email.
     * @param email The email of the user
     * @param phoneNumber The new phone number
     */
    public void updatePhoneNumber(
            String email,
            String phoneNumber,
            Consumer<Void> onSuccess,
            Consumer<Exception> onException) {

        // Gets an account based on the email
        DocumentReference docRef = accountsRef.document(email);

        //      The following code is from the firebase docs on how to update a field in the DB:
        //      https://firebase.google.com/docs/firestore/manage-data/add-data#update-data
        docRef.update("phoneNumber", phoneNumber)
                .addOnSuccessListener(onSuccess::accept)
                .addOnFailureListener(onException::accept);
    }
}
