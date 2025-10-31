package com.example.evently;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.evently.data.model.Event;
import com.example.evently.data.model.Account;
import com.example.evently.databinding.FragmentEventDetailsBinding;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import java.util.Date;


public class EventDetailsFragment extends Fragment {
    private FragmentEventDetailsBinding binding;

    Event event;
    ArrayList<Account> entrants;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentEventDetailsBinding.inflate(getLayoutInflater(), container, false);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        entrants = new ArrayList<Account>();

        addDummyData();

        loadEventInformation(event, entrants.size());

        loadEntrants(entrants);
    }

    public void loadEventInformation(Event event, int entrantNumber) {
        TextView eventName = binding.eventName;
        TextView image = binding.eventPicture;
        TextView desc = binding.eventDescription;
        TextView entrantCount = binding.entryCount;


        eventName.setText(event.name());
        desc.setText(event.description());
        entrantCount.setText(String.valueOf(entrantNumber));
    }

    public void loadEntrants(ArrayList<Account> entrants)
    {
        RecyclerView entrantList = binding.entrantList;
        entrantList.setLayoutManager(new LinearLayoutManager(this.getContext()));
        entrantList.setAdapter(new EntrantListAdapter(this.getContext(), entrants));
    }

    public void addDummyData() {
        event = new Event(
                "Sample Event Name",
                "Blah Blah Blah Description",
                new Date(),
                new Date(),
                UUID.randomUUID(),
                Optional.of((long) 100),
                10);

        entrants.add(new Account("Email 1@gmail.com", "Name 1", Optional.of("780"), "Email 10@gmail.com"));
        entrants.add(new Account("Email 2@gmail.com", "Name 2", Optional.empty(), "Emtail 20@gmail.com"));

        for (int i = 0; i < 15; i++)
        {
            int num = i*10;
            String n = String.valueOf(num);
            String email = "Email " + n + "@gmail.com";
            String name = "Name " + n;
            Account a = new Account(email, name, Optional.empty(), email +" v");
            entrants.add(a);
        }
    }
}
