package com.example.evently.data;

import java.util.Optional;
import java.util.UUID;
import java.util.Set;
import java.util.HashSet;
import com.example.evently.data.model.Account;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import android.util.Log;

public class AccountDB {
    
    private final CollectionReference accountsRef;
    private Set<Account> accounts = new HashSet<Account>();

    /**
     * Sets up the database of accounts. 
     * Sources: CMPUT 301 Lab 5 Presentation
     * @param db The firestore db containing the accounts
     */
    public AccountDB(FirebaseFirestore db){

        // Saves the accounts collection for later use
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

                    // Gets all the information from the 
                    UUID accountID = (UUID) snapshot.get("AccountID");
                    String name = snapshot.getString("name");
                    String email = snapshot.getString("email");
                    Optional<String> phoneNumber = (Optional<String>) snapshot.get("phoneNumber");
                    String hashedPassword = snapshot.getString("hashedPassword");

                    accounts.add(new Account(accountID, name, email, phoneNumber, hashedPassword));
                }
            }
        });
    }




    
}
