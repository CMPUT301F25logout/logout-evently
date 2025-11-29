package com.example.evently.ui.common;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.evently.data.EventsDB;
import com.example.evently.databinding.FragmentEntrantBinding;

/**
 * Recycler view that displays each entrant as a row with their profile picture and name
 * @Author Vinson Lou
 */
public class EntrantRecyclerViewAdapter
        extends RecyclerView.Adapter<EntrantRecyclerViewAdapter.EntrantViewHolder> {
    private final List<String> entrants;
    private final boolean showRemoveButton;
    private UUID eventID;

    public EntrantRecyclerViewAdapter() {
        this.entrants = new ArrayList<>();
        showRemoveButton = false;
    }

    public EntrantRecyclerViewAdapter(List<String> entrants) {
        this.entrants = entrants;
        showRemoveButton = false;
    }

    public EntrantRecyclerViewAdapter(
            List<String> entrants, boolean showRemoveButton, UUID eventID) {
        this.entrants = entrants;
        this.showRemoveButton = showRemoveButton;
        this.eventID = eventID;
    }

    public static class EntrantViewHolder extends RecyclerView.ViewHolder {
        public final FragmentEntrantBinding binding;

        public EntrantViewHolder(FragmentEntrantBinding binding) {
            super(binding.getRoot());
            // Can define click listeners here
            this.binding = binding;
        }
    }

    @NonNull @Override
    public EntrantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new EntrantViewHolder(FragmentEntrantBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(final EntrantViewHolder holder, int position) {
        // Only set the names if the entrant list is not empty
        if (entrants.isEmpty()) {
            return;
        }
        var binding = holder.binding;

        // Define the names
        String name = entrants.get(position);

        // Set the name of each person
        binding.entrantName.setText(name);

        // If we need to have the remove button, it is shown
        if (showRemoveButton) {
            // Sets visibility
            binding.removeButton.setVisibility(View.VISIBLE);
            // Sets Onclick listener.

            binding.removeButton.setOnClickListener(v -> {
                EventsDB eventsDB = new EventsDB();
                eventsDB.addCancelled(eventID, name)
                        .alongside(eventsDB.unSelect(eventID, name))
                        .thenRun(x -> {
                            entrants.remove(position);
                            notifyItemRemoved(position);
                        });
            });
        }
    }

    @Override
    public int getItemCount() {
        return entrants.size();
    }
}
