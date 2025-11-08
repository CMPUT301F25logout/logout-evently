package com.example.evently;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.example.evently.data.AccountDB;
import com.example.evently.data.model.Account;

@RunWith(AndroidJUnit4.class)
public class AccountDatabaseTest extends FirebaseEmulatorTest {
    @After
    public void cleanUpAccounts() throws ExecutionException, InterruptedException {
        new AccountDB().nuke().await();
    }

    /**
     * The following code tests the store, and fetch account operations.
     */
    @Test
    public void testStoreAndFetchAccount() throws InterruptedException, ExecutionException {
        AccountDB db = new AccountDB();
        // Creates an account, and stores it in the DB
        Account addedAccount = new Account(
                "hi@gmail.com",
                "AlexBradley",
                Optional.of("123-456-7890"),
                "my_visible_email@yahoo.com");
        db.storeAccount(addedAccount).await();

        // The following code fetches the added account, and confirms the query returns the result
        final var fetchedAccount = db.fetchAccount(addedAccount.email()).await();

        assertTrue(fetchedAccount.isPresent());
        assertEquals(fetchedAccount.get(), addedAccount);
    }

    /**
     * The following code tests the rename phone, and email functions.
     */
    @Test
    public void testRenamePhoneAndEmail() throws InterruptedException, ExecutionException {
        AccountDB db = new AccountDB();

        // Creates an account, and stores it in the DB
        Account addedAccount = new Account(
                "hi@gmail.com",
                "AlexBradley",
                Optional.of("123-456-7890"),
                "my_visible_email@yahoo.com");
        db.storeAccount(addedAccount).await();

        // The following code tests the update phone number method, and confirms the query returns
        // the result
        String newPhoneNumber = "my_new_phone!!!!";

        final var optionalAccount = db.updatePhoneNumber("hi@gmail.com", newPhoneNumber)
                .alongside(db.fetchAccount("hi@gmail.com"))
                .await();
        // Checks that the fetched account is found
        assertTrue(optionalAccount.isPresent());

        Account fetchedAccount = optionalAccount.get();
        assertEquals(newPhoneNumber, fetchedAccount.phoneNumber().orElse(null));

        // The following code tests the update visible email method, and confirms the query returns
        // the visible email.
        String newEmail = "NEW EMAIL WOOOOOOO!!!!";
        final var fetchedOptionalAccount = db.updateVisibleEmail("hi@gmail.com", newEmail)
                .alongside(db.fetchAccount("hi@gmail.com"))
                .await();

        assertTrue(fetchedOptionalAccount.isPresent());
        Account fetchedAccount1 = fetchedOptionalAccount.get();

        assertEquals(newEmail, fetchedAccount1.visibleEmail());

        assertTrue(true);
    }

    /**
     * The following code tests the delete account function. It first creates an account, stores it,
     * deletes it, and ensures it is deleted.
     */
    @Test
    public void testDeleteAccount() throws InterruptedException, ExecutionException {
        AccountDB db = new AccountDB();

        // Creates an account, and stores it in the DB
        Account addedAccount = new Account(
                "hi@gmail.com",
                "AlexBradley",
                Optional.of("123-456-7890"),
                "my_visible_email@yahoo.com");
        db.storeAccount(addedAccount).await();

        db.deleteAccount(addedAccount.email()).await();

        final var optionalAccount = db.fetchAccount("hi@gmail.com").await();
        assertFalse(optionalAccount.isPresent());
    }
}
