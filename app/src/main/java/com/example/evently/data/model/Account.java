package com.example.evently.data.model;

import java.util.Optional;
import java.util.UUID;

/**
 * Represents a profile of a person. 
 * @param name The name of the account
 * @param email The email of the account holder
 * @param phoneNumber An optional phone number for the account holder
 * @param hashedPassword A hashed version of the user's password. 
 */
public record Account(
    UUID accountID,
    String name,
    String email,
    Optional<String> phoneNumber,
    Integer hashedPassword
) {}
