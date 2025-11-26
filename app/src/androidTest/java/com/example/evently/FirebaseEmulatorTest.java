package com.example.evently;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import androidx.test.platform.app.InstrumentationRegistry;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

import com.example.evently.data.AccountDB;
import com.example.evently.data.generic.Promise;
import com.example.evently.data.model.Account;
import com.example.evently.utils.FirebaseAuthUtils;

/**
 * Class that sets up firebase emulator integration for use by all tests.
 * This class will use the standard emulator ports for both firebase and android emulator.
 * It is recommended that all tests that use firebase extend this class.
 * @apiNote This will not work with real devices. Only an AVD.
 */
public abstract class FirebaseEmulatorTest {
    private static final String defaultMockEmail = "foobar589@gmail.com";

    /**
     * The default mock account that will be created and used to login.
     * Additional accounts (not for auth) may be added via extraMockAccounts.
     */
    protected static final Account defaultMockAccount =
            new Account(defaultMockEmail, "Foo Bar", Optional.of("7801234579"), defaultMockEmail);

    /**
     * Extending classes may override this to demand additional starting accounts.
     * @return List of additional accounts the emulator should be initialized with.
     * @apiNote The emulator always starts with one specific mock account that is used for authentication.
     */
    public List<Account> extraMockAccounts() {
        return new ArrayList<>();
    }

    @BeforeClass
    public static void setUpEmulator() throws InterruptedException, ExecutionException {
        // Connect to the emulators (unless specified otherwise).
        if (BuildConfig.HOOK_EMULATOR) {
            try {
                FirebaseAuth.getInstance().useEmulator("10.0.2.2", 9099);
                FirebaseFirestore.getInstance().useEmulator("10.0.2.2", 8080);
                FirebaseStorage.getInstance().useEmulator("10.0.2.2", 9199);
            } catch (IllegalStateException e) {
                // Emulators have already been set up.
            }
        }
        // Set up the default mock account + authenticate.
        // Register in firebase auth.
        final var instrumentation = InstrumentationRegistry.getInstrumentation();
        try {
            FirebaseAuthUtils.dumbLogin(instrumentation.getContext(), defaultMockEmail, true)
                    .await();
        } catch (ExecutionException execExc) {
            // We may safely ignore collision (it's already created).
            if (!(execExc.getCause() instanceof FirebaseAuthUserCollisionException)) {
                // Rethrow for any other type of execution exception.
                throw execExc;
            }
        }
        // Login with this account.
        FirebaseAuthUtils.dumbLogin(instrumentation.getContext(), defaultMockEmail, false)
                .await();
    }

    @Before
    public void setUpAccounts() throws ExecutionException, InterruptedException {
        // Create the mock accounts and register them in our DB.
        final var accountDB = new AccountDB();
        final var accounts = this.extraMockAccounts();
        accounts.add(defaultMockAccount);

        Promise.all(accounts.stream().map(accountDB::storeAccount)).await();
    }

    @After
    public void tearDownAccount() throws ExecutionException, InterruptedException {
        // Remove the mock accounts from DB.
        final var accounts = this.extraMockAccounts();
        final var accountDB = new AccountDB();
        accounts.add(defaultMockAccount);

        Promise.all(accounts.stream().map(acc -> accountDB.deleteAccount(acc.email())))
                .await();
    }

    /**
     * Helper to be used for tests that manually sign out.
     */
    protected void signBackIn() throws ExecutionException, InterruptedException {
        final var instrumentation = InstrumentationRegistry.getInstrumentation();
        FirebaseAuthUtils.dumbLogin(instrumentation.getContext(), defaultMockEmail, false)
                .await();
    }
}
