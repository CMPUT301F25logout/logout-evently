package com.example.evently.ui.common;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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

    public EntrantRecyclerViewAdapter(List<String> entrants, UUID eventID) {
        this.entrants = entrants;
        showRemoveButton = true;
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

        // TODO: Add a remove button with default invisibility to the XML

        if (showRemoveButton) {

            // TODO: Change visibility in this section

            // TODO Add an onclick listener to delete the user from the selectedEntrants,
            //  and move to canceled

        }

        // Define the names
        String name = entrants.get(position);

        // Set the name of each person
        binding.entrantName.setText(name);
    }

    @Override
    public int getItemCount() {
        return entrants.size();
    }
}
