package com.example.evently.ui.common;

import java.util.ArrayList;
import java.util.List;

import android.view.LayoutInflater;
import android.view.View;
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

    public EntrantRecyclerViewAdapter() {
        this.entrants = new ArrayList<>();
        showRemoveButton = false;
    }

    public EntrantRecyclerViewAdapter(List<String> entrants) {
        this.entrants = entrants;
        showRemoveButton = false;
    }

    public EntrantRecyclerViewAdapter(List<String> entrants, boolean showRemoveButton) {
        this.entrants = entrants;
        this.showRemoveButton = showRemoveButton;
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

                // The following line of code is from Google, Gemini 3 Pro
                // "I have a recycler view adapter, and I am removing items from the recycler view
                // when a button within each recycler view item is clicked.
                //
                // I am currently trying to fix the remove button from my binding. The first clicked
                // item is deleting, but then when another item is clicked, it is removing an item
                // other than the one I clicked. How to fix this?"
                int current_pos = holder.getBindingAdapterPosition();
                String email = entrants.get(current_pos);

                cancelEntrant(email);
            });
        }
    }

    /**
     * Function to remove an item from selected, and move to canceled. Does nothing unless
     * overridden.
     * @param email the email of the user being canceled
     */
    public void cancelEntrant(String email) {}

    @Override
    public int getItemCount() {
        return entrants.size();
    }
}
