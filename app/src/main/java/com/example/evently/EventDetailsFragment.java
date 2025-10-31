package com.example.evently;

import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

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

import com.example.evently.data.model.Account;
import com.example.evently.data.model.Event;
import com.example.evently.databinding.FragmentEventDetailsBinding;

/**
 * Fragment that displays the event information as well as the entrants that have been waitlisted.
 *
 *
 * Things to implement:
 * Images for the event and accounts
 * QR Code
 * Extending the description if it's too long
 *
 * Layout: fragment_event_details.xml
 */
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

    /**
     * Loads the event information into the fragment
     * @param event The event object to load into the page
     * @param entrantNumber The number of entrants to display the amount of people that entered
     */
    public void loadEventInformation(Event event, int entrantNumber) {
        TextView eventName = binding.eventName;
        TextView image = binding.eventPicture;
        TextView desc = binding.eventDescription;
        TextView entrantCount = binding.entryCount;

        String entrantCountStr = String.valueOf(entrantNumber);

        // Display according information depending on if the event has an entrant limit
        if (event.entrantLimit().isPresent()) {
            entrantCountStr = String.valueOf(entrantNumber) + "/"
                    + String.valueOf(event.entrantLimit().get());
        }

        entrantCount.setText(entrantCountStr);
        eventName.setText(event.name());
        desc.setText(event.description());
    }

    /**
     * Loads the entrants into the fragment using a recycler view
     * Uses the event_entrants_list_content.xml for the recycler view rows
     * @param entrants The list of entrants for this given event
     */
    public void loadEntrants(ArrayList<Account> entrants) {
        RecyclerView entrantList = binding.entrantList;
        entrantList.setLayoutManager(new LinearLayoutManager(this.getContext()));
        entrantList.setAdapter(new EntrantListAdapter(this.getContext(), entrants));
    }

    /**
     * Adds dummy data for visualizing the fragment
     */
    public void addDummyData() {
        event = new Event(
                "Sample Event Name",
                "Blah Blah Blah Description",
                new Date(),
                new Date(),
                UUID.randomUUID(),
                Optional.of((long) 100),
                // Optional.empty(),
                10);

        entrants.add(new Account(
                "Email 1@gmail.com", "Name 1", Optional.of("780"), "Email 10@gmail.com"));
        entrants.add(new Account(
                "Email 2@gmail.com", "Name 2", Optional.empty(), "Emtail 20@gmail.com"));

        for (int i = 0; i < 15; i++) {
            int num = i * 10;
            String n = String.valueOf(num);
            String email = "Email " + n + "@gmail.com";
            String name = "Name " + n;
            Account a = new Account(email, name, Optional.empty(), email + " v");
            entrants.add(a);
        }
    }
}
