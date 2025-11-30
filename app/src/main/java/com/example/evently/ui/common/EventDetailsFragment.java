package com.example.evently.ui.common;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.storage.StorageReference;

import com.example.evently.R;
import com.example.evently.data.EventsDB;
import com.example.evently.data.model.Event;
import com.example.evently.databinding.FragmentEventDetailsBinding;
import com.example.evently.ui.model.EventViewModel;
import com.example.evently.utils.GlideUtils;

/**
 * Fragment that displays the event information as well as the entrants that have been waitlisted.
 * <p>
 * Things to implement:
 * Images for the event and accounts
 * Extending the description if it's too long
 * <p>
 * Layout: fragment_event_details.xml
 * @author Vinson Lou
 */
public abstract class EventDetailsFragment<E extends Fragment, A extends Fragment>
        extends Fragment {
    protected FragmentEventDetailsBinding binding;
    protected UUID eventID;

    private static final DateTimeFormatter EVENT_DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());
    protected EventViewModel eventViewModel;

    /**
     * Implementors should note which fragment to fill in the entrant list fragment container.
     * @apiNote This fragment should use the view models scoped in this fragment to obtain event related data.
     * @return Class of the fragment.
     */
    protected abstract Class<E> getFragmentForEntrantListContainer();

    /**
     * Implementors should note which fragment to fill in the action buttons fragment container.
     * @apiNote This fragment should use the view models scoped in this fragment to obtain event related data.
     * @return Class of the fragment.
     */
    protected abstract Class<A> getFragmentForActionButtonsContainer();

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentEventDetailsBinding.inflate(getLayoutInflater(), container, false);

        eventViewModel = new ViewModelProvider(this).get(EventViewModel.class);

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

        eventViewModel.getEventLive().observe(getViewLifecycleOwner(), this::loadEventInformation);
        eventViewModel.getEventEntrantsLive().observe(getViewLifecycleOwner(), eventEntrants -> {
            binding.currentEntrantCount.setText(
                    String.valueOf(eventEntrants.all().size()));
        });

        // Load the entrants list fragment if we were not recreated.
        if (savedInstanceState != null) return;

        getChildFragmentManager()
                .beginTransaction()
                .setReorderingAllowed(true)
                .add(R.id.entrantListContainer, getFragmentForEntrantListContainer(), null)
                .commit();
        // Also load the action buttons fragment.
        getChildFragmentManager()
                .beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.actionButtonsContainer, getFragmentForActionButtonsContainer(), null)
                .commit();
    }

    /**
     * Loads the event information into the fragment
     * @param event The event object to load into the page
     */
    private void loadEventInformation(Event event) {
        binding.eventName.setText(event.name());
        binding.eventDescription.setText(event.description());
        binding.eventCategory.setText(event.category().toString());

        // Sets the seat text:
        String seatLimit = String.valueOf(event.selectionLimit());
        binding.seatsText.setText(seatLimit);

        // If we have a limit, it is shown
        if (event.optionalEntrantLimit().isPresent()) {
            binding.waitlistSeparator.setVisibility(View.VISIBLE);
            binding.entrantLimit.setVisibility(View.VISIBLE);
            // Sets the entrant limit text.
            String entrantLimit = event.optionalEntrantLimit().get().toString();
            binding.entrantLimit.setText(entrantLimit);
        } else {
            binding.waitlistSeparator.setVisibility(View.INVISIBLE);
            binding.entrantLimit.setVisibility(View.INVISIBLE);
        }

        // Formats the date, and stores it in the selection date text
        String formattedDate =
                EVENT_DATE_TIME_FORMATTER.format(event.selectionTime().toInstant());
        binding.selectionDateText.setText(formattedDate);

        eventID = event.eventID();

        // Loads the picture into the image view.
        StorageReference posterRef = new EventsDB().getPosterStorageRef(eventID);
        GlideUtils.loadPosterIntoImageView(posterRef, binding.eventPicture);

        event.optionalEntrantLimit().ifPresent(limit -> {
            binding.entrantLimitSection.setVisibility(View.VISIBLE);
            binding.entrantLimit.setText(String.valueOf(limit));
        });
    }
}
