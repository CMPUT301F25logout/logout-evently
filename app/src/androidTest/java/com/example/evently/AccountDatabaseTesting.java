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
//
//
//    public Account getMockAccount(){
//        return AccountDB.createAccount(
//                "Alex Bradley",
//                "hi@gmail.com",
//                Optional.of("123-456-7890"),
//                "password"
//        );
//    }
//
//    @Test
//    public void testAddAccount() {
//        AccountDB db = new AccountDB();
//        Account a = getMockAccount();
//        db.storeAccount(a);
//        db.fetchAccount(a.accountID());
//
//        // Updates the email.
//        db.updateEmail(a.accountID(),"Aadsfhjkasdf@gmail.com");
//        db.updatePhoneNumber(a.accountID(),"");
//        assertTrue(true);
//    }

}
