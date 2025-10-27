package com.example.evently;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

import android.util.Log;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.evently.data.AccountDB;
import com.example.evently.data.model.Account;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

/**
 * TODO: Finish AccountDatabaseTesting.
 */

@RunWith(AndroidJUnit4.class)
@LargeTest
public class AccountDatabaseTesting {

//    @Rule
//    public ActivityScenarioRule<MainActivity> scenario = new
//            ActivityScenarioRule<MainActivity>(MainActivity.class);

//    public AccountDB createDB(){
//        // Creates accountDB
//        AccountDB accountDB = new AccountDB();
//
//        // Stores the account
//        accountDB.storeAccount(getMockAccount());
//        return accountDB;
//    }

//    public Account getMockAccount(){
//        return AccountDB.createAccount(
//                "Alex Bradley",
//                "hi@gmail.com",
//                "123-456-7890",
//                "password"
//        );
//    }

    @Test
    public void testAddAccount() {
//        AccountDB db = createDB();
//        Account a = getMockAccount();
//        db.storeAccount(a);

//        Optional<Account> fetched_account = db.fetchAccount(a.accountID());
//        if (fetched_account.isPresent()) {
//            assertEquals(a.accountID(), fetched_account.get().accountID());
//            Log.d("Fetched account", "testAddAccount: "+ fetched_account.get().accountID());
//        }

        assertTrue(true);


    }

}
