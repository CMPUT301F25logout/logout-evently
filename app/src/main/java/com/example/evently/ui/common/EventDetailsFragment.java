package com.example.evently.ui.common;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.evently.R;
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
 * @Author Vinson Lou
 */
public class EventDetailsFragment extends Fragment {
    private FragmentEventDetailsBinding binding;

    UUID eventID;
    private Event event;
    private List<String> entrants;
    private EntrantListAdapter entrantsAdapter;

    private Boolean joined = false;

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

        eventsDB.fetchEvent(eventID)
                .optionally(event -> eventsDB.fetchEventEntrants(Collections.singletonList(eventID))
                        .thenRun(eventEntrants -> {
                            final var eventEntrantsInfo = eventEntrants.get(0);
                            loadEventInformation(event, eventEntrantsInfo.all().size());
                            loadEntrants(eventEntrantsInfo.all());
                            entrants = eventEntrantsInfo.all();
                        }));

        // Back Button logic
        Button back = view.findViewById(R.id.buttonBack);
        back.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());

        // TODO Implement logic to determine whether or not the user has joined the event from the
        // database by comparing the users name to see if it's in the entrant list (Use joined)

        // Waitlist Action button pressed
        Button wlAction = view.findViewById(R.id.waitlistAction);
        wlAction.setOnClickListener(v -> updateEventInformation());
    }

    /**
     * Update the information on the fragment to reflect the user leaving or joining the waitlist.
     * Updates information on the database as well. (Maybe?)
     * Need Account information to update the list accordingly
     */
    public void updateEventInformation() {

        // If the user is on the waitlist list, leave the waitlist, update information accordingly
        if (joined) {
            // Update the list of entrants and notifyDataSetChanged();
            entrantsAdapter.notifyDataSetChanged();
        }
        // If the user is not on the waitlist, join the waitlist, update information accordingly
        else {
            // Update the list of entrants and notifyDataSetChanged();
            entrantsAdapter.notifyDataSetChanged();
        }

        joined = !joined;
        displayWaitlistAction();
        updateDB();
    }

    /**
     * Updates information on the database.
     */
    public void updateDB() {}

    /**
     * Loads the event information into the fragment
     * @param event The event object to load into the page
     * @param currEntrants The number of entrants to display the amount of people that entered
     */
    public void loadEventInformation(Event event, int currEntrants) {
        TextView eventName = binding.eventName;
        ImageView image = binding.eventPicture;
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

        // Update with respective information
        displayWaitlistAction();
        entrantCount.setText(entrantCountStr);
        eventName.setText(event.name());
        desc.setText(event.description());
    }

    /**
     * Change the display of the Button on the waitlist depending on if the user joined the event or not
     */
    // Not sure if we plan on updating the page or just the Entrant List and the button
    // Whenever the user joins the event, so it's a function for now
    public void displayWaitlistAction() {
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
        entrantsAdapter = new EntrantListAdapter(entrants);
        entrantList.setAdapter(entrantsAdapter);
    }
}
