package com.example.evently.ui.common;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.evently.data.EventsDB;
import com.example.evently.data.model.Event;
import com.example.evently.databinding.FragmentEventDetailsBinding;

/**
 * Fragment that displays the event information as well as the entrants that have been waitlisted.
 *
 * TODO Use the eventID string to get the event from the database
 * Things to implement:
 * Images for the event and accounts
 * QR Code
 * Extending the description if it's too long
 *
 * Layout: fragment_event_details.xml
 */
public class EventDetailsFragment extends Fragment {
    private FragmentEventDetailsBinding binding;

    UUID eventID;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentEventDetailsBinding.inflate(getLayoutInflater(), container, false);

        // Receive the event ID string
        eventID = UUID.fromString(
                EventDetailsFragmentArgs.fromBundle(getArguments()).getEventId());

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

        final var eventsDB = new EventsDB();

        eventsDB.fetchEvent(
                eventID,
                event -> {
                    if (event.isEmpty()) {
                        // This should never happen.
                        Log.w("EventDetailsFragment", "Received non existent event ID: " + eventID);
                        NavHostFragment.findNavController(this).navigateUp();
                        return;
                    }

                    eventsDB.fetchEventEntrants(
                            Collections.singletonList(eventID),
                            eventEntrants -> {
                                final var eventEntrantsInfo = eventEntrants.get(0);
                                loadEventInformation(
                                        event.get(), eventEntrantsInfo.all().size(), true);
                                loadEntrants(eventEntrantsInfo.all());
                            },
                            e -> {
                                Log.e("EventDetails", e.toString());
                            });
                },
                e -> {
                    Log.e("EventDetails", e.toString());
                });
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
        if (event.optionalEntrantLimit().isPresent()) {
            entrantCountStr = currEntrants + "/" + event.optionalEntrantLimit().get();

            // Disable the button if the waitlist is already full
            if (event.optionalEntrantLimit().get() == currEntrants) {
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
    public void displayWaitlistAction(boolean joined) {
        Button waitlistAction = binding.waitlistAction;
        String wlActionText;
        if (joined) {
            wlActionText = "LEAVE WAITLIST";
        } else {
            wlActionText = "JOIN WAITLIST";
        }

        waitlistAction.setText(wlActionText);
    }

    /**
     * Loads the entrants into the fragment using a recycler view
     * Uses the event_entrants_list_content.xml for the recycler view rows
     * @param entrants The list of entrants for this given event
     */
    public void loadEntrants(List<String> entrants) {
        RecyclerView entrantList = binding.entrantList;
        entrantList.setLayoutManager(new LinearLayoutManager(this.getContext()));
        entrantList.setAdapter(new EntrantListAdapter(entrants));
    }
}
