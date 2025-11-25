package com.example.evently.ui.common;

import java.util.UUID;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.ObjectKey;
import com.google.firebase.storage.StorageReference;

import com.example.evently.R;
import com.example.evently.data.EventsDB;
import com.example.evently.data.model.Event;
import com.example.evently.databinding.FragmentEventDetailsBinding;
import com.example.evently.ui.model.EventViewModel;

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

        if (savedInstanceState == null) {
            // Load the entrants list fragment if we were not recreated.
            getChildFragmentManager()
                    .beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.entrantListContainer, getFragmentForEntrantListContainer(), null)
                    .commit();
            // Also load the action buttons fragment.
            getChildFragmentManager()
                    .beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.actionButtonsContainer, getFragmentForActionButtonsContainer(), null)
                    .commit();
        }
    }

    /**
     * Loads the event information into the fragment
     * @param event The event object to load into the page
     */
    private void loadEventInformation(Event event) {
        binding.eventName.setText(event.name());
        binding.eventDescription.setText(event.description());
        binding.eventCategory.setText(event.category().toString());
        eventID = event.eventID();

        StorageReference posterRef = new EventsDB().getPosterStorageRef(eventID);

        // The following code attempts to find the posterRef in the DB, and store it into the event
        // picture. android.R.drawable.ic_menu_report_image is used while searching or if the image
        // is not found in the DB.
        //
        // Additionally, the following question was asked to Google, Gemini 3 Pro:
        // "I am using Glide for showing images from firebase storage in my Java android app, but my
        // images are not updating when the image in firebase storage are changed. Do you know how
        // to change this?"
        // This resulted in adding a signature.
        Glide.with(getContext())
                .load(posterRef)
                .placeholder(android.R.drawable.ic_menu_report_image)
                .error(android.R.drawable.ic_menu_report_image)
                .signature(new ObjectKey(System.currentTimeMillis())) // Forces when image changes
                .into(binding.eventPicture);

        event.optionalEntrantLimit().ifPresent(limit -> {
            binding.entrantLimitSection.setVisibility(View.VISIBLE);
            binding.entrantLimit.setText(String.valueOf(limit));
        });
    }
}
