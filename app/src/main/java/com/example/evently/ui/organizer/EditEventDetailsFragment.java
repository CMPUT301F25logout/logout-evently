package com.example.evently.ui.organizer;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;

import com.example.evently.data.EventsDB;
import com.example.evently.ui.common.EventDetailsFragment;
import com.example.evently.utils.GlideUtils;

public class EditEventDetailsFragment
        extends EventDetailsFragment<EventMetaFragment, OrganizerEventActionsFragment> {

    private ImageView imageView;

    // The following code defines a launcher to pick a picture. For more details, see the android
    // photo picker docs:
    // https://developer.android.com/training/data-storage/shared/photo-picker
    private final ActivityResultLauncher<PickVisualMediaRequest> pickPoster =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri == null || super.eventID == null) {
                    Log.d("Poster Picker", "No poster selected");
                    return;
                }
                new EventsDB().storePoster(super.eventID, uri).thenRun(x -> {
                    // Stores it into the image, and shows it if still in the fragment
                    if (getContext() == null) return;
                    Glide.with(getContext()).load(uri).into(imageView);
                    Toast.makeText(getContext(), "Poster Updated!", Toast.LENGTH_SHORT)
                            .show();
                });
            });

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        imageView = super.binding.eventPicture;

        // Launches the poster picker when image is clicked.
        imageView.setOnClickListener(v -> {
            pickPoster.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        });
    }

    @Override
    protected Class<EventMetaFragment> getFragmentForEntrantListContainer() {
        return EventMetaFragment.class;
    }

    @Override
    protected Class<OrganizerEventActionsFragment> getFragmentForActionButtonsContainer() {
        return OrganizerEventActionsFragment.class;
    }
}
