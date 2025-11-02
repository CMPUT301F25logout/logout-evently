package com.example.evently.ui.common;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.evently.data.model.Account;
import com.example.evently.data.model.Category;
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
    List<Account> entrants;

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

        // This is temporary until the event ID is implemented
        addDummyData();

        // TODO Implement logic to determine whether or not the user has joined the event from the database

        loadEventInformation(event, entrants.size(), false);

        loadEntrants(entrants);
    }

    /**
     * Loads the event information into the fragment
     * @param event The event object to load into the page
     * @param currEntrants The number of entrants to display the amount of people that entered
     */
    public void loadEventInformation(Event event, int currEntrants, boolean joined) {
        TextView eventName = binding.eventName;
        TextView image = binding.eventPicture;
        TextView desc = binding.eventDescription;
        TextView entrantCount = binding.entryCount;
        Button waitlistAction = binding.waitlistAction;

        String entrantCountStr = String.valueOf(currEntrants);

        // Display according information depending on if the event has an entrant limit
        if (event.entrantLimit().isPresent()) {
            entrantCountStr = String.valueOf(currEntrants) + "/"
                    + String.valueOf(event.entrantLimit().get());

            // Disable the button if the waitlist is already full
            if (event.entrantLimit().get() == currEntrants)
            {
                waitlistAction.setEnabled(false);
                // Change the button text to indicate that it's full
                String waitlistFull = "Event is full.";
                waitlistAction.setText(waitlistFull);
            }
        }

        displayWaitlistAction(joined);

        entrantCount.setText(entrantCountStr);
        eventName.setText(event.name());
        desc.setText(event.description());
    }

    /**
     * Change the display of the Button on the waitlist depending on if the user joined the event or not
     * @param joined Whether or not the user has joined the event
     */
    // Not sure if we plan on updating the page or just the Entrant List and the button
    // Whenever the user joins the event, so it's a function for now
    public void displayWaitlistAction(boolean joined)
    {
        Button waitlistAction = binding.waitlistAction;
        String wlActionText;
        if (joined)
        {
            wlActionText = "LEAVE WAITLIST";
        }
        else
        {
            wlActionText = "JOIN WAITLIST";
        }

        waitlistAction.setText(wlActionText);
    }

    /**
     * Loads the entrants into the fragment using a recycler view
     * Uses the event_entrants_list_content.xml for the recycler view rows
     * @param entrants The list of entrants for this given event
     */
    public void loadEntrants(List<Account> entrants) {
        RecyclerView entrantList = binding.entrantList;
        entrantList.setLayoutManager(new LinearLayoutManager(this.getContext()));
        entrantList.setAdapter(new EntrantListAdapter(entrants));
    }

    /**
     * Adds dummy data for visualizing the fragment, need to implement the onclick for the events to remove this
     */
    public void addDummyData() {
        event = new Event(
                "Sample Event Name",
                "Blah Blah Blah Description",
                Instant.now(),
                Instant.now(),
                UUID.randomUUID(),
                Optional.of((long) 100),
                // Optional.empty(),
                10,
                Category.SPORTS);

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
