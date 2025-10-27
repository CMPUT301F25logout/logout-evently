package com.example.evently.data;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

import com.example.evently.data.model.Account;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import android.util.Log;

import androidx.annotation.NonNull;

public class AccountDB {

    // Reference to the accounts collection
    private final CollectionReference accountsRef;

    /**
     * Sets up the database of accounts.
     * Sources: CMPUT 301 Lab 5 Presentation
     */
    public AccountDB(){
        // Defines the reference to the accounts collection
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        accountsRef = db.collection("accounts");
    }

    /**
     * Creates an account based on specified parameters
     * @param name The name of the account
     * @param email The email of the account
     * @param phoneNumber The phone number of the account.
     * @param password The password of the account
     * @return The newly created account.
     */
    public static Account createAccount(String name, String email, String phoneNumber, String password){

        // If a string is passed to the function, it is placed in the optionalPhoneNum variable
        Optional<String> optionalPhoneNum;
        if (phoneNumber != null){
            optionalPhoneNum = Optional.of(phoneNumber);
        }
        // If no phoneNum is passed, we use Optional.empty().
        else{
            optionalPhoneNum = Optional.empty();
        }

        // Creates the account, and returns it. Account is not added to the list of accounts.
        return new Account(
                UUID.randomUUID(),
                name,
                email,
                optionalPhoneNum,
                password.hashCode()
        );
    }

    /**
     * Stores an account in the database
     * @param a The account stored in the database.
     * @return The task from storing an account
     */
    public Task<Void> storeAccount(Account a){
        DocumentReference docRef = accountsRef.document(a.accountID().toString());

        // An Optional<String> cannot be stored in the DB.
        String storable_phone_num = a.phoneNumber().toString();

        HashMap<String,Object> dataToStore = new HashMap<>();
        dataToStore.put("name",a.name());
        dataToStore.put("email",a.email());
        dataToStore.put("phoneNumber",storable_phone_num);
        dataToStore.put("hashedPassword",a.hashedPassword());

        return docRef.set(dataToStore)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                      @Override
                      public void onSuccess(Void aVoid) {
                          Log.d("Firestore", "DocumentSnapshot successfully written!");
                      }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("Firestore", "DocumentSnapshot not written!");
                    }
                });
    }


    /**
     * Returns an account based b
     * @param accountID The id of the target account
     * @return A task with the Document snapshot
     */
    public Task<DocumentSnapshot> fetchAccount(UUID accountID) {

        // Returns a document snapshot with the attached account.
        String fetchAccountString = accountID.toString();
        return accountsRef.document(fetchAccountString).get();
    }


    /**
     * Delete an account from the database by UUID
     * @param accountID The UUID of the target account
     * @return The delete account task.
     */
    public Task<Void> deleteAccount(UUID accountID){

        /**
         * The following code is from the firebase documentation on deleting documents:
         * https://firebase.google.com/docs/firestore/manage-data/delete-data
         */
        return accountsRef.document(accountID.toString())
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("DEL ACCOUNT", "DocumentSnapshot successfully deleted!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("DEL ACCOUNT", "Error deleting document", e);
                    }
                }
        );
    }

    /**
     * Updates an account phone number in the database
     * @param phoneNumber The new phone number
     * @return The update phoneNumber task.
     */
    public Task<Void> updatePhoneNumber(UUID accountID,String phoneNumber){

        // Gets an account by its document number
        String stringAccountID = accountID.toString();
        DocumentReference docRef = accountsRef.document(stringAccountID);

//      The following code is from the firebase docs on how to update a field in the DB:
//      https://firebase.google.com/docs/firestore/manage-data/add-data#update-data
        return docRef.update("phoneNumber",phoneNumber);
    }


    /**
     * Updates an account's email in the database
     * @param email The new email of the account holder
     * @return The update email task.
     */
    public Task<Void> updateEmail(UUID accountID,String email){

        // Gets an account by its document number
        String stringAccountID = accountID.toString();
        DocumentReference docRef = accountsRef.document(stringAccountID);

//      The following code is from the firebase docs on how to update a field in the DB:
//      https://firebase.google.com/docs/firestore/manage-data/add-daSta#update-data
        return docRef.update("phoneNumber",email);
    }
}
