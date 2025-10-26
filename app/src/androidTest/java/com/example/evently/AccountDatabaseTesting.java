package com.example.evently;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.evently.data.AccountDB;
import com.example.evently.data.model.Account;

/**
 * TODO: Finish AccountDatabaseTesting.
 */

@RunWith(AndroidJUnit4.class)
@LargeTest
public class AccountDatabaseTesting {

    @Rule
    public ActivityScenarioRule<MainActivity> scenario = new
            ActivityScenarioRule<MainActivity>(MainActivity.class);

    public AccountDB createDB(){
        // Creates accountDB
        AccountDB accountDB = new AccountDB();

        // Stores the account
        accountDB.storeAccount(getMockAccount());
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

    @Test
    public void testAddAccount(){
        AccountDB db = createDB();
        Account a = getMockAccount();

        assertTrue(db.storeAccount(a));

    }

}
