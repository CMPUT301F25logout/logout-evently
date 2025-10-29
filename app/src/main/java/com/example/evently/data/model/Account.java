package com.example.evently.data.model;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.HashMap;
import java.util.Optional;

/**
 * Represents a profile of a person.
 * @author alexander-b
 *
 * @param email The email of the account holder
 * @param name The name of the account
 * @param phoneNumber An optional phone number for the account holder
 * @param isAdmin A boolean representing whether or not an account is an Admin account.
 */
public record Account(String email, String name, Optional<String> phoneNumber, Boolean isAdmin) {

    /**
     * Converts an account to a hashMap for storing in the DB. Since the email
     * is the primary key of the account, it is not added to the hashMap
     * @return A hashmap of the account's contents.
     */
    public HashMap<String, Object> toHashMap() {

        // Creates the hashMap
        HashMap<String, Object> hashMap = new HashMap<>();

        // Stores the account in the hashMap
        hashMap.put("name", this.name());
        hashMap.put("phoneNumber", this.phoneNumber().orElse(null));
        hashMap.put("isAdmin", this.isAdmin());
        return hashMap;
    }

    /**
     * Returns an account from a document snapshot.
     * @param documentSnapshot The snapshot of the account
     * @return the fetched account.
     */
    public static Account getAccountFromSnapshot(DocumentSnapshot documentSnapshot)
            throws NullPointerException {

        String stringPhoneNum = documentSnapshot.getString("phoneNumber");
        Optional<String> optionalPhoneNum = Optional.ofNullable(stringPhoneNum);

        // Creates and stores the account.
        return new Account(
                documentSnapshot.getString("email"),
                documentSnapshot.getString("name"),
                optionalPhoneNum,
                documentSnapshot.getBoolean("isAdmin"));
    }
}
