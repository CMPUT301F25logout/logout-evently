package com.example.evently;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.BeforeClass;

public abstract class FirebaseEmulatorTest {
    @BeforeClass
    public static void setUpEmulators() {
        FirebaseFirestore.getInstance().useEmulator("10.0.2.2", 8080);
    }
}
