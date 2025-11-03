package com.example.evently;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;

import android.util.Log;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.example.evently.data.AccountDB;
import com.example.evently.data.model.Account;

@RunWith(AndroidJUnit4.class)
public class AccountDatabaseTest extends FirebaseEmulatorTest {
    @Rule
    public ActivityScenarioRule<MainActivity> scenario =
            new ActivityScenarioRule<MainActivity>(MainActivity.class);

    /**
     * The following code tests the store, and fetch account operations.
     */
    @Test
    public void testStoreAndFetchAccount() throws InterruptedException {

        // The idea for using CountDownLatches for synchronization is from the article below:
        // Article: https://stackoverflow.com/questions/15938538/how-can-i-make-a-junit-test-wait
        // Title: "How can I make a JUnit test wait?"
        // Answer: https://stackoverflow.com/a/64645442
        // License: CC BY-SA 4.0
        CountDownLatch addAccountLatch = new CountDownLatch(1);

        AccountDB db = new AccountDB();
        // Creates an account, and stores it in the DB
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
                fetchedAccount -> {
                    fetchLatch.countDown();

                    assertTrue(fetchedAccount.isPresent());
                    assertEquals(fetchedAccount.get(), addedAccount);
                },
                e -> {
                    Log.d("FETCH ACCOUNT", "testStoreAccount: Failed to fetch account");
                });

        fetchLatch.await();
        assertTrue(true);
    }

    /**
     * The following code tests the rename phone, and email functions.
     */
    @Test
    public void testRenamePhoneAndEmail() throws InterruptedException {

        CountDownLatch addAccountLatch = new CountDownLatch(1);
        AccountDB db = new AccountDB();

        // Creates an account, and stores it in the DB
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
                            optionalAccount -> {

                                // Checks that the fetched account is found
                                assertTrue(optionalAccount.isPresent());

                                Account fetchedAccount = optionalAccount.get();
                                assertEquals(
                                        newPhoneNumber,
                                        fetchedAccount.phoneNumber().orElse(null));
                                phoneNumLatch.countDown();
                            },
                            e -> {});
                },
                e -> {});
        phoneNumLatch.await();

        // The following code tests the update visible email method, and confirms the query returns
        // the visible email.
        CountDownLatch visibleEmailLatch = new CountDownLatch(1);
        String newEmail = "NEW EMAIL WOOOOOOO!!!!";
        db.updateVisibleEmail(
                "hi@gmail.com",
                newEmail,
                v -> {
                    db.fetchAccount(
                            "hi@gmail.com",
                            fetchedOptionalAccount -> {
                                assertTrue(fetchedOptionalAccount.isPresent());
                                Account fetchedAccount = fetchedOptionalAccount.get();

                                assertEquals(newEmail, fetchedAccount.visibleEmail());
                                visibleEmailLatch.countDown();
                            },
                            e -> {});
                },
                e -> {});
        visibleEmailLatch.await();

        assertTrue(true);
    }

    /**
     * The following code tests the delete account function. It first creates an account, stores it,
     * deletes it, and ensures it is deleted.
     */
    @Test
    public void testDeleteAccount() throws InterruptedException {
        AccountDB db = new AccountDB();

        // The idea for using CountDownLatches for synchronization is from the article below:
        // Article: https://stackoverflow.com/questions/15938538/how-can-i-make-a-junit-test-wait
        // Title: "How can I make a JUnit test wait?"
        // Answer: https://stackoverflow.com/a/64645442
        // License: CC BY-SA 4.0
        CountDownLatch addAccountLatch = new CountDownLatch(1);

        // Creates an account, and stores it in the DB
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

        // Remove the account from the DB.
        CountDownLatch delAccountLatch = new CountDownLatch(1);
        db.deleteAccount(
                addedAccount.email(),
                v -> {
                    delAccountLatch.countDown();
                },
                e -> {});
        delAccountLatch.await(); // Waits until the account is added

        // The following code ensures the account cannot be found.
        CountDownLatch fetchLatch = new CountDownLatch(1);

        db.fetchAccount(
                "hi@gmail.com",
                optionalAccount -> {
                    assertFalse(optionalAccount.isPresent());
                    fetchLatch.countDown();
                },
                e -> {});
        fetchLatch.await();
    }
}
