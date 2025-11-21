package com.example.evently;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import androidx.test.core.app.ActivityScenario;

import com.google.firebase.auth.FirebaseAuth;
import org.junit.Before;
import org.junit.Test;

import com.example.evently.data.AccountDB;
import com.example.evently.data.model.Account;
import com.example.evently.ui.entrant.EntrantActivity;
import com.example.evently.utils.FirebaseAuthUtils;

public class FirebaseAuthUtilsTest extends FirebaseEmulatorTest {
    /**
     * Makes sure user is logged in before tests start
     */
    @Before
    public void confirmLogin() throws ExecutionException, InterruptedException {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null) return;
        setUpEmulator();
    }

    /**
     * Tests that getCurrentEmail returns proper auth email
     */
    @Test
    public void getCurrentEmailTest() {
        assertEquals(FirebaseAuthUtils.getCurrentEmail(), defaultMockAccount.email());
    }

    /**
     * Tests sign out is successfully completed
     */
    @Test
    public void signOutTest() {
        FirebaseAuth.AuthStateListener listener =
                firebaseAuth -> assertNull(firebaseAuth.getCurrentUser());

        FirebaseAuth.getInstance().addAuthStateListener(listener);

        FirebaseAuthUtils.signOut(task -> assertTrue(task.isSuccessful()));
        FirebaseAuth.getInstance().removeAuthStateListener(listener);
    }

    /**
     * Tests account is properly deleted
     */
    @Test
    public void testDeleteAccount() throws InterruptedException, ExecutionException {
        AccountDB db = new AccountDB();
        ActivityScenario<EntrantActivity> scenario = ActivityScenario.launch(EntrantActivity.class);
        FirebaseAuthUtils.testRun = true;
        CountDownLatch latch = new CountDownLatch(1);

        scenario.onActivity(
                activity -> FirebaseAuthUtils.deleteAccount(activity, v -> latch.countDown(), e -> {
                    latch.countDown();
                    fail(e.toString());
                }));

        latch.await();

        Optional<Account> acc =
                db.fetchAccount(FirebaseEmulatorTest.defaultMockAccount.email()).await();
        assertNull(FirebaseAuth.getInstance().getCurrentUser());
        assertTrue(acc.isEmpty());

        FirebaseEmulatorTest.setUpEmulator(); // Crashes if this isn't here

        if (!latch.await(5, TimeUnit.SECONDS)) fail("Timeout waiting for deleteAccount");
    }
}
