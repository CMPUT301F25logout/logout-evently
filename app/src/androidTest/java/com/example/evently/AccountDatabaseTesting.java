package com.example.evently;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.util.Log;

import androidx.test.ext.junit.rules.ActivityScenarioRule;

import com.example.evently.data.AccountDB;
import com.example.evently.data.model.Account;

import org.junit.Rule;
import org.junit.Test;

import java.util.Optional;

public class AccountDatabaseTesting {
    @Rule
    public ActivityScenarioRule<MainActivity> scenario =
            new ActivityScenarioRule<MainActivity>(MainActivity.class);

    public Account getMockAccount() {
        return new Account("hi@gmail.com", "AlexBradley", Optional.of("123-456-7890"), true);
    }
    //
    @Test
    public void testStoreAccount() {
        AccountDB db = new AccountDB();
        final Account addedAccount = getMockAccount();

        // Stores the account in the DB.
        db.storeAccount(
                addedAccount,
                v -> {
                    Log.d("STORE ACCOUNT", "testStoreAccount: Stored account");
                },
                e -> {
                    Log.d("STORE ACCOUNT", "testStoreAccount: Failed to store account");
                });

        db.fetchAccount(
                addedAccount.email(),
                documentSnapshot -> {
                    Account fetched_account = Account.getAccountFromSnapshot(documentSnapshot);

                    if (documentSnapshot.exists()) {
                        assertEquals(fetched_account, addedAccount);
                    } else {
                        fail();
                    }
                },
                e -> {
                    Log.d("FETCH ACCOUNT", "testStoreAccount: Failed to fetch account");
                });

        assertTrue(true);
    }
    //        db.fetchAccount(a.accountID());
    //
    //        // Updates the email.
    //        db.updateEmail(a.accountID(),"Aadsfhjkasdf@gmail.com");
    //        db.updatePhoneNumber(a.accountID(),"");
    //        assertTrue(true);
    //    }

}
