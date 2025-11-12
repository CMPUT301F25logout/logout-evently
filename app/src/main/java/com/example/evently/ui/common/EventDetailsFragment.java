package com.example.evently.ui.common;

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

import com.example.evently.R;
import com.example.evently.data.EventsDB;
import com.example.evently.data.model.Event;
import com.example.evently.databinding.FragmentEventDetailsBinding;
import com.example.evently.utils.FirebaseAuthUtils;

/**
 * Fragment that displays the event information as well as the entrants that have been waitlisted.
 * <p>
 * Things to implement:
 * Images for the event and accounts
 * QR Code
 * Extending the description if it's too long
 * <p>
 * Layout: fragment_event_details.xml
 * @author Vinson Lou
 */
public abstract class EventDetailsFragment<F extends Fragment> extends Fragment {
    private FragmentEventDetailsBinding binding;

    /**
     * Override this function if the resulting fragment shouldn't display the waitlist button.
     * @return Whether or not to display the "join/leave waitlist" button.
     */
    protected boolean shouldDisplayActionBtn() {
        return true;
    }

    /**
     * Implementors should note which fragment to fill in the fragment container.
     * @return Class of the fragment.
     */
    protected abstract Class<F> getFragmentForEntrantListContainer();

    /**
     * Implementors should return the eventID they may get passed via navigation args.
     * Must be a trivial getter.
     * @return event id for the associated event.
     */
    protected abstract UUID getEventID();

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

        final var eventsDB = new EventsDB();

        final var eventID = getEventID();
        eventsDB.fetchEvent(eventID).optionally(event -> eventsDB.fetchEventEntrants(eventID)
                .optionally(eventEntrantsInfo -> {
                    // TODO (chase): Decouple. Event information loading SHOULD NOT need
                    // EventEntrants.
                    // Only the entrants fragment should need it.
                    final var joined =
                            eventEntrantsInfo.all().contains(FirebaseAuthUtils.getCurrentEmail());
                    loadEventInformation(event, eventEntrantsInfo.all().size(), joined);
                }));
        loadEntrants();

        // Back Button logic
        Button back = view.findViewById(R.id.buttonBack);
        back.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());
    }

    /**
     * Loads the event information into the fragment
     * @param event The event object to load into the page
     * @param currEntrants The number of entrants to display the amount of people that entered
     */
    public void loadEventInformation(Event event, int currEntrants, boolean joined) {
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

        if (shouldDisplayActionBtn()) {
            displayWaitlistAction(joined);
        }

        entrantCount.setText(entrantCountStr);
        eventName.setText(event.name());
        desc.setText(event.description());
    }

    /**
     * Change the display of the Button on the waitlist depending on if the user joined the event or not
     * @param joined Whether or not the user has joined the event
     */
    public void displayWaitlistAction(boolean joined) {
        Button waitlistAction = binding.waitlistAction;
        binding.waitlistAction.setVisibility(View.VISIBLE);
        waitlistAction.setEnabled(true);
        String wlActionText;
        if (joined) {
            wlActionText = "LEAVE WAITLIST";
        } else {
            wlActionText = "JOIN WAITLIST";
        }

        waitlistAction.setText(wlActionText);
        waitlistAction.setOnClickListener(v -> {
            waitlistAction.setEnabled(false);
            if (joined) {
                new EventsDB()
                        .unenroll(getEventID(), FirebaseAuthUtils.getCurrentEmail())
                        .thenRun(vu -> {
                            displayWaitlistAction(false);
                            reloadEntrants();
                        });
            } else {
                new EventsDB()
                        .enroll(getEventID(), FirebaseAuthUtils.getCurrentEmail())
                        .thenRun(vu -> {
                            displayWaitlistAction(true);
                            reloadEntrants();
                        });
            }
        });
    }

    /**
     * Loads the entrant list fragment which handles the entrant lists to show.
     */
    public void loadEntrants() {
        // Load the recycler view fragment with event ID.
        final var bundle = new Bundle();
        bundle.putSerializable("eventID", getEventID());
        getChildFragmentManager()
                .beginTransaction()
                .setReorderingAllowed(true)
                .add(R.id.entrantListContainer, getFragmentForEntrantListContainer(), bundle)
                .commit();
    }

    /**
     * Reloads the entrant list
     */
    public void reloadEntrants() {
        // Load the recycler view fragment with event ID.
        final var bundle = new Bundle();
        bundle.putSerializable("eventID", getEventID());
        getChildFragmentManager()
                .beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.entrantListContainer, getFragmentForEntrantListContainer(), bundle)
                .commit();
    }
}
