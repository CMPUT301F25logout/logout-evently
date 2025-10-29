package com.example.evently.data.model;

import java.util.HashMap;
import java.util.Optional;

/**
 * Represents a profile of a person.
 * @author alexander-b
 *
 * @param email The email of the account holder
 * @param name The name of the account
 * @param phoneNumber An optional phone number for the account holder
 * @param visibleEmail The visible email for a user.
 */
public record Account(
        String email, String name, Optional<String> phoneNumber, String visibleEmail) {

    // Converts an account to a hashMap for storing in the DB. Since the email
    // is the primary key of the account, it is not added to the hashMap
    // @return A hashmap of the account's contents.
    public HashMap<String, Object> toHashMap() {

        // Creates the hashMap
        HashMap<String, Object> hashMap = new HashMap<>();

        // Stores the account in the hashMap
        hashMap.put("name", this.name);
        hashMap.put("phoneNumber", this.phoneNumber.orElse(null));
        hashMap.put("visibleEmail", this.visibleEmail);
        return hashMap;
    }
}
