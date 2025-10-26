package com.example.evently.data;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;
import java.util.Set;
import java.util.HashSet;
import com.example.evently.data.model.Account;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import android.util.Log;

import androidx.annotation.NonNull;

public class AccountDB {

    // Reference to the accounts collection
    private final CollectionReference accountsRef;

    private final Set<Account> accounts = new HashSet<Account>();

    /**
     * Sets up the database of accounts. 
     * Sources: CMPUT 301 Lab 5 Presentation
     */
    public AccountDB(){

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        accountsRef = db.collection("accounts");
        // Sets up the accounts snapshot listener, which updates the set of records.
        accountsRef.addSnapshotListener( (value, error) -> {

            // If we have a firestore error, the error is logged
            if (error != null) Log.e("Firestore", error.toString());

            // If the firestore has changed, we empty out set of accounts, and update it with
            // the records from the firestore. 
            if(value != null && !value.isEmpty()){
                
                accounts.clear();
                for (QueryDocumentSnapshot snapshot : value){

                    // Gets all the information from the firestore
                    String accountID = snapshot.getId();
                    String name = snapshot.getString("name");
                    String email = snapshot.getString("email");

                    String phoneNumber = snapshot.getString("phoneNumber");
                    Optional<String> optionalPhone = Optional.ofNullable(phoneNumber);

                    Integer hashedPassword = snapshot.getLong("hashedPassword").intValue();
                    Log.d("hashed password", "Hashed Password: " + hashedPassword.toString());
                    Log.d("hashed password", "accountID : " + accountID);
                    accounts.add(new Account(UUID.fromString(accountID), name, email, optionalPhone, hashedPassword));
                    Log.d("hashed password", "Add successful!: " + hashedPassword.toString());
                }
            }
        });
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
     */
    public boolean storeAccount(Account a){
        DocumentReference docRef = accountsRef.document(a.accountID().toString());

        // An Optional<String> cannot be stored in the DB.
        String storable_phone_num = a.phoneNumber().toString();

        HashMap<String,Object> dataToStore = new HashMap<>();
        dataToStore.put("name",a.name());
        dataToStore.put("email",a.email());
        dataToStore.put("phoneNumber",storable_phone_num);
        dataToStore.put("hashedPassword",a.hashedPassword());

        docRef.set(dataToStore)
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

        return true;
        // Since the database has a snapshot listener, I believe no point adding to the hashset.
    }

    /**
     * Gets the number of accounts in the database.
     * @return Number of accounts in the database.
     */
    public int getNumberAccounts(){
        return accounts.size();
    }

    /**
     * Fetches an account from a provided accountID.
     * @param accountID The id of the target account
     * @return An optional of the Account
     */
    public Optional<Account> fetchAccount(UUID accountID){

        String fetchAccountString = accountID.toString();

        // Searches through each account in the account set, and returns account if it matches the
        // account id.
        for (Account a : accounts){

            // If the current account being searched is a string, we
            String currentAccountID = a.accountID().toString();
            if (currentAccountID.equals(fetchAccountString)) return Optional.of(a);

        }
        return Optional.empty();
    }

    // TODO: (Alex) Clean up code, and add "deleteAccount" method.

}
