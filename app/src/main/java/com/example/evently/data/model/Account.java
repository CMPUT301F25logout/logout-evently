package com.example.evently.data.model;

import java.util.Optional;
import java.util.UUID;

/**
 * Represents a profile of a person.
 * @author alexander-b
 *
 * @param accountID The UUID of the account
 * @param name The name of the account
 * @param email The email of the account holder
 * @param phoneNumber An optional phone number for the account holder
 * @param hashedPassword A hashed version of the user's password.
 * @param isAdmin A boolean representing whether or not an account is an Admin account.
 */
public record Account(
        UUID accountID,
        String name,
        String email,
        Optional<String> phoneNumber,
        Integer hashedPassword,
        Boolean isAdmin) {}
