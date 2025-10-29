package com.example.evently;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.util.Log;

import androidx.test.ext.junit.rules.ActivityScenarioRule;

import com.example.evently.data.AccountDB;
import com.example.evently.data.model.Account;

import org.junit.Rule;
import org.junit.Test;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;

public class AccountDatabaseTesting {
    @Rule
    public ActivityScenarioRule<MainActivity> scenario =
            new ActivityScenarioRule<MainActivity>(MainActivity.class);

    /**
     * The following code tests the store, and fetch account operations.
     * @throws InterruptedException
     */
    @Test
    public void testStoreAndFetchAccount() throws InterruptedException {

        /**
         * The idea for using CountDownLatches for synchronization is from the article below:
         * Article: https://stackoverflow.com/questions/15938538/how-can-i-make-a-junit-test-wait
         * Title: "How can I make a JUnit test wait?"
         * Answer: https://stackoverflow.com/a/64645442
         * License: CC BY-SA 4.0
         */
        CountDownLatch addAccountLatch = new CountDownLatch(1);

        // Creates an account, and stores it in the DB
        AccountDB db = new AccountDB();
        Account addedAccount = new Account(
                "hi@gmail.com",
                "AlexBradley",
                Optional.of("123-456-7890"),
                "my_visible_email@yahoo.com");
        db.storeAccount(
                addedAccount,
                v -> {
                    addAccountLatch.countDown();
                },
                e -> {});
        addAccountLatch.await(); // Waits until the account is added

        // The following code fetches the added account, and confirms the query returns the result
        CountDownLatch fetchLatch = new CountDownLatch(1);
        db.fetchAccount(
                addedAccount.email(),
                documentSnapshot -> {
                    Account fetched_account = Account.getAccountFromSnapshot(documentSnapshot);
                    fetchLatch.countDown();
                    assertEquals(fetched_account, addedAccount);
                },
                e -> {
                    Log.d("FETCH ACCOUNT", "testStoreAccount: Failed to fetch account");
                });

        fetchLatch.await();
        assertTrue(true);
    }

    /**
     * The following code tests the rename phone, and email functions.
     * @throws InterruptedException
     */
    @Test
    public void testRenamePhoneAndEmail() throws InterruptedException {

        /**
         * The idea for using CountDownLatches for synchronization is from the article below:
         * Article: https://stackoverflow.com/questions/15938538/how-can-i-make-a-junit-test-wait
         * Title: "How can I make a JUnit test wait?"
         * Answer: https://stackoverflow.com/a/64645442
         * License: CC BY-SA 4.0
         */
        CountDownLatch addAccountLatch = new CountDownLatch(1);

        // Creates an account, and stores it in the DB
        AccountDB db = new AccountDB();
        Account addedAccount = new Account(
                "hi@gmail.com",
                "AlexBradley",
                Optional.of("123-456-7890"),
                "my_visible_email@yahoo.com");
        db.storeAccount(
                addedAccount,
                v -> {
                    addAccountLatch.countDown();
                },
                e -> {});
        addAccountLatch.await(); // Waits until the account is added

        // The following code tests the update phone number method, and confirms the query returns
        // the result
        CountDownLatch phoneNumLatch = new CountDownLatch(1);
        String newPhoneNumber = "my_new_phone!!!!";
        db.updatePhoneNumber(
                "hi@gmail.com",
                newPhoneNumber,
                v -> {
                    db.fetchAccount(
                            "hi@gmail.com",
                            documentSnapshot -> {
                                Account fetched_account =
                                        Account.getAccountFromSnapshot(documentSnapshot);

                                assertEquals(
                                        fetched_account.phoneNumber().orElse(null), newPhoneNumber);
                                phoneNumLatch.countDown();
                            },
                            e -> {});
                },
                e -> {});
        phoneNumLatch.await();

        // The following code tests the update email method, and confirms the query returns the
        // result
        CountDownLatch visibleEmailLatch = new CountDownLatch(1);
        String newEmail = "NEW EMAIL WOOOOOOO!!!!";
        db.updateVisibleEmail(
                "hi@gmail.com",
                newEmail,
                v -> {
                    db.fetchAccount(
                            "hi@gmail.com",
                            documentSnapshot -> {
                                Account fetched_account =
                                        Account.getAccountFromSnapshot(documentSnapshot);

                                assertEquals(
                                        fetched_account.phoneNumber().orElse(null), newPhoneNumber);
                                visibleEmailLatch.countDown();
                            },
                            e -> {});
                },
                e -> {});
        visibleEmailLatch.await();

        assertTrue(true);
    }
}
