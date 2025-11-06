package com.example.evently;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import android.Manifest;
import androidx.test.rule.GrantPermissionRule;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.firestore.FirebaseFirestore;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;

import com.example.evently.data.AccountDB;
import com.example.evently.data.model.Account;

/**
 * Class that sets up firebase emulator integration for use by all tests.
 * This class will use the standard emulator ports for both firebase and android emulator.
 * It is recommended that all tests that use firebase extend this class.
 * @apiNote This will not work with real devices. Only an AVD.
 */
public abstract class FirebaseEmulatorTest {
    private static final String defaultMockEmail = "foobar589@gmail.com";
    private static final String mockPassword = "password";

    /**
     * If mockAccounts is not overridden, this is the only account that is created.
     */
    protected static final Account mockAccount =
            new Account(defaultMockEmail, "Foo Bar", Optional.of("7801234579"), defaultMockEmail);

    /**
     * Extending classes may override this to demand different starting accounts.
     * @return List of accounts the emulator should be initialized with.
     */
    public List<Account> mockAccounts() {
        final var accounts = new ArrayList<Account>();
        accounts.add(mockAccount);
        return accounts;
    }

    @Rule
    public GrantPermissionRule permissionRule =
            GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS);

    @BeforeClass
    public static void setUpEmulator() throws ExecutionException, InterruptedException {
        // Connect to the emulators.
        try {
            FirebaseAuth.getInstance().useEmulator("10.0.2.2", 9099);
            FirebaseFirestore.getInstance().useEmulator("10.0.2.2", 8080);
        } catch (IllegalStateException e) {
            // Emulators have already been set up.
        }
    }

    @Before
    public void setUpAccounts() throws ExecutionException, InterruptedException {
        // Create mock accounts and register them in our DB.
        final var accounts = this.mockAccounts();

        // Register in firebase auth.
        final var auth = FirebaseAuth.getInstance();
        final var authTasks = accounts.stream()
                .map(acc -> auth.createUserWithEmailAndPassword(acc.email(), mockPassword))
                .collect(Collectors.toList());

        try {
            Tasks.await(Tasks.whenAllSuccess(authTasks));
        } catch (ExecutionException execExc) {
            if (!(execExc.getCause() instanceof FirebaseAuthUserCollisionException)) {
                // Rethrow for any other type of execution exception.s
                throw execExc;
            }
        }

        // Register these accounts in the DB.
        final var accountDB = new AccountDB();
        for (final var acc : accounts) {
            accountDB.storeAccount(acc);
        }
    }

    /**
     * Login with the default account and plug it into FirebaseAuth as "currentUser.
     */
    public static void login() throws ExecutionException, InterruptedException {
        login(defaultMockEmail);
    }

    /**
     * Login with a specified account so FirebaseAuth now has that set as "currentUser".
     * @param email Email of the account from mockAccounts (if custom) to sign in with.
     */
    public static void login(String email) throws ExecutionException, InterruptedException {
        final var auth = FirebaseAuth.getInstance();
        Tasks.await(auth.signInWithEmailAndPassword(email, mockPassword));
    }
}
