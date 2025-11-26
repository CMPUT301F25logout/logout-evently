package com.example.evently.ui.admin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.evently.utils.GlideUtils;
import com.google.firebase.storage.StorageReference;

import com.example.evently.data.EventsDB;
import com.example.evently.data.model.Event;
import com.example.evently.databinding.FragmentImageRowBinding;

/**
 * {@link RecyclerView.Adapter} that can display an {@link Event}'s poster.
 * <p>
 */
public class ImageRecyclerViewAdapter
        extends RecyclerView.Adapter<ImageRecyclerViewAdapter.ImageViewHolder> {

    EventsDB eventsDB;

    /**
     * Interface for the callback of the delete button click
     */
    public interface ImageOnClickListener {
        void accept(UUID a);
    }

    private final Map<UUID, StorageReference> images;
    private final ImageOnClickListener onImageClick;

    public ImageRecyclerViewAdapter(
            Map<UUID, StorageReference> images, ImageOnClickListener onImageClick) {
        this.images = images;
        this.onImageClick = onImageClick;
        this.eventsDB = new EventsDB();
    }

    @NonNull @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ImageRecyclerViewAdapter.ImageViewHolder(FragmentImageRowBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        // Only set the images if the dictionary is not empty
        if (images.isEmpty()) {
            return;
        }

        // Convert the keys to a list
        List<UUID> eventIDs = new ArrayList<>(images.keySet());

        holder.eventID = eventIDs.get(position);
        var binding = holder.binding;

        // Set the image of the eventID
        StorageReference poster = eventsDB.getPosterStorageRef(holder.eventID);
        GlideUtils.loadPosterIntoImageView(poster, binding.eventPoster);

        // Set the text of the image description
        eventsDB.fetchEvent(holder.eventID).thenRun(event -> {
            String description = event.get().name() + " Event Poster";
            binding.imageDescription.setText(description);
        });

        // Set the remove image button click listener
        binding.btnRemove.setOnClickListener(v -> onImageClick.accept(holder.eventID));
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    /**
     * ViewHolder that holds the layout binding and an UUID representing the eventID
     */
    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        public final FragmentImageRowBinding binding;
        public UUID eventID;

        public ImageViewHolder(FragmentImageRowBinding binding) {
            super(binding.getRoot());

            this.binding = binding;
        }
    }
}
