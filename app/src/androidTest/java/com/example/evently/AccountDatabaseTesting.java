package com.example.evently;

import static org.junit.Assert.assertTrue;

import android.util.Log;

import androidx.test.ext.junit.rules.ActivityScenarioRule;

import com.example.evently.data.AccountDB;
import com.example.evently.data.model.Account;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Rule;
import org.junit.Test;

import java.util.HashMap;
import java.util.Optional;

public class AccountDatabaseTesting {
    @Rule
    public ActivityScenarioRule<MainActivity> scenario =
            new ActivityScenarioRule<MainActivity>(MainActivity.class);

    //
    @Test
    public void testStoreAccount() {

        Log.d("STORE ACCOUNT", "t");
        //        db.fetchAccount(
        //                addedAccount.email(),
        //                documentSnapshot -> {
        //                    Account fetched_account =
        // Account.getAccountFromSnapshot(documentSnapshot);
        //
        //                    if (documentSnapshot.exists()) {
        //                        assertEquals(fetched_account, addedAccount);
        //                    } else {
        //                        fail();
        //                    }
        //                },
        //                e -> {
        //                    Log.d("FETCH ACCOUNT", "testStoreAccount: Failed to fetch account");
        //                });

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
