package com.example.evently.ui.admin;

import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.evently.ui.common.ConfirmFragmentNoInput;
import com.google.firebase.storage.StorageReference;

import com.example.evently.R;
import com.example.evently.data.EventsDB;
import com.example.evently.ui.common.ConfirmDeleteDialog;

/**
 * A fragment representing a list of event posters/images the admin can browse and interact with.
 */
public class AdminBrowseImagesFragment extends Fragment {

    private ImageRecyclerViewAdapter adapter;
    private RecyclerView recyclerView;

    UUID selectedEventPoster;
    Map<UUID, StorageReference> posterDictionary;

    private final EventsDB eventsDB = new EventsDB();

    /**
     * Handles clicks on an image row in the Admin Browse Images.
     * <p>
     * Opens a dialog for the admin to interact with.
     * @param eventID The event ID of the event poster that was interacted with
     */
    protected void onImageClick(UUID eventID) {
        // Show the confirm deletion dialog
        selectedEventPoster = eventID;

        ConfirmFragmentNoInput confirmFragment = ConfirmFragmentNoInput.newInstance(
                "Delete Image",
                "Are you sure you want to delete this image?");
        confirmFragment.show(getParentFragmentManager(), "confirmNoInput");
        getParentFragmentManager()
                .setFragmentResultListener(
                        ConfirmFragmentNoInput.requestKey, this, this::onDialogConfirmClick
                );
    }

    /**
     * Supplies the Browse list with all event posters
     * @param callback Callback that will be passed the dictionary of eventID and its poster storage reference.
     */
    protected void initImages(Consumer<Map<UUID, StorageReference>> callback) {
        eventsDB.fetchAllPosters().thenRun(callback).catchE(e -> {
            Log.e("BrowseImages", e.toString());
            Toast.makeText(requireContext(), "Something went wrong...", Toast.LENGTH_SHORT)
                    .show();
        });
    }

    @Nullable @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        // Inflate the recycler view using the layout of fragment_admin_images.xml
        recyclerView =
                (RecyclerView) inflater.inflate(R.layout.fragment_admin_images, container, false);

        if (recyclerView == null) {
            throw new AssertionError("AdminBrowseImagesFragment called with non recyclerview.");
        }

        Context context = recyclerView.getContext();
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        // Set up the images with the recycler view
        initImages(eventDictionary -> {
            adapter = new ImageRecyclerViewAdapter(eventDictionary, this::onImageClick);
            posterDictionary = eventDictionary;
            recyclerView.setAdapter(adapter);
        });

        return recyclerView;
    }

    /**
     * The dialog closed with a positive click (confirm).
     * Delete the event poster from the eventsDB and update the dictionary as well as the adapter.
     */
    public void onDialogConfirmClick(String s, Bundle bundle) {
        // Delete the event poster
        eventsDB.deleteEvent(selectedEventPoster);

        // Update the list and update the adapter
        posterDictionary.remove(selectedEventPoster);
        adapter.notifyDataSetChanged();

        Toast.makeText(requireContext(), "Image deleted.", Toast.LENGTH_SHORT).show();
    }

}
