package com.example.evently;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.evently.data.AccountDB;
import com.example.evently.data.model.Account;
import com.google.api.LogDescriptor;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Optional;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        AccountDB db = new AccountDB();

        db.storeAccount(new Account("hi@gmail.com", "AlexBradley",
                Optional.of("123-456-7890"), true));

        db.storeAccount(new Account("hello@gmail.com", "AlexBradley",
                Optional.empty(), true));

        db.fetchAccount("hi@gmail.com", documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Account found_account = Account.getAccountFromSnapshot(documentSnapshot);

//                db.deleteAccount(found_account.email());
//                Log.d("ACCOUNT DEL", "onCreate: Account deleted");

                db.storeAccount(found_account);
                Log.d("ACCOUNT Add", "onCreate: Account Added");
            }
        }, e -> {}
        );


        db.deleteAccount("hello@gmail.com");

    }
}
