package com.example.evently;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.concurrent.ExecutionException;

import com.google.firebase.auth.FirebaseAuth;
import org.junit.Test;

import com.example.evently.utils.FirebaseAuthUtils;

public class FirebaseAuthUtilsTest extends FirebaseEmulatorTest {
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
    public void signOutTest() throws InterruptedException, ExecutionException {
        FirebaseAuthUtils.signOut();
        assertNull(FirebaseAuth.getInstance().getCurrentUser());
        signBackIn();
    }
}
