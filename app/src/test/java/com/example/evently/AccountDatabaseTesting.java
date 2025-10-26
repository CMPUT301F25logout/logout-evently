package com.example.evently;

import org.junit.*;
import static org.junit.Assert.*;

import com.example.evently.data.AccountDB;
import com.example.evently.data.model.Account;

/**
 * TODO: Finish AccountDatabaseTesting.
 */
public class AccountDatabaseTesting {


    public AccountDB createDB(){
        // Creates accountDB
        AccountDB accountDB = new AccountDB();

        // Stores the account
        accountDB.storeAccount(
                AccountDB.createAccount(
                "Alex Bradley",
                "hi@gmail.com",
                "123-456-7890",
                "password")
        );
        return accountDB;
    }

    public Account getMockAccount(){
        return AccountDB.createAccount(
                "Alex Bradley",
                "hi@gmail.com",
                "123-456-7890",
                "password"
        );
    }

}
