package com.example.evently;

import com.google.firebase.firestore.FirebaseFirestore;
import org.junit.BeforeClass;

/**
 * Class that sets up firebase emulator integration for use by all tests.
 * This class will use the standard emulator ports for both firebase and android emulator.
 * It is recommended that all tests that use firebase extend this class.
 * @apiNote This will not work with real devices. Only an AVD.
 */
public abstract class FirebaseEmulatorTest {
    @BeforeClass
    public static void setUpEmulators() {
        FirebaseFirestore.getInstance().useEmulator("10.0.2.2", 8080);
    }
}
