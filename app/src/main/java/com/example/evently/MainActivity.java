package com.example.evently;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.evently.data.model.Event;
import com.example.evently.data.model.MockUser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.example.evently.ui.auth.AuthActivity;
import com.example.evently.ui.auth.SignOutFragment;

public class MainActivity extends AppCompatActivity {

    Event event;
    ArrayList<MockUser> entrants;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.fragment_event_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.event_details), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        entrants = new ArrayList<MockUser>();

        addDummyData();

        loadEventInformation(event, entrants);
    }



    public void loadEventInformation(Event event, ArrayList<MockUser> entrants)
    {
        TextView eventName = findViewById(R.id.eventName);
        TextView image = findViewById(R.id.eventPicture);
        TextView desc = findViewById(R.id.eventDescription);
        TextView entrantCount = findViewById(R.id.entryCount);
        RecyclerView entrantList = findViewById(R.id.entrantList);

        eventName.setText(event.name());
        desc.setText(event.description());
        entrantCount.setText(String.valueOf(entrants.size()));

        entrantList.setLayoutManager(new LinearLayoutManager(this));
        entrantList.setAdapter(new EntrantListAdapter(this, entrants));

    }

    public void addDummyData()
    {
        event = new Event("J", "Blah Blah Blah Description", new Date(), new Date(), UUID.randomUUID(), Optional.of((long)100), 10);

        entrants.add(new MockUser("U", "First Image"));
        entrants.add(new MockUser("L", "Second Image"));
    }

}
