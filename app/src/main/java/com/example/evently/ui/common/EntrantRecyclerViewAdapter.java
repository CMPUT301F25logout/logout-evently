package com.example.evently.ui.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.evently.databinding.FragmentEntrantBinding;

/**
 * Recycler view that displays each entrant as a row with their profile picture and name
 */
public class EntrantRecyclerViewAdapter
        extends RecyclerView.Adapter<EntrantRecyclerViewAdapter.EntrantViewHolder> {

    /**
     * Listener to set on the "remove entrant" button active during cancelled entrants list.
     */
    @FunctionalInterface
    public interface OnRemoveButtonClickListener {
        void onRemoveButtonClick(String email);
    }

    private final Map<String, String> entrants;
    private boolean showRemoveButton = false;
    private OnRemoveButtonClickListener removeButtonListener = ignored -> {};

    public EntrantRecyclerViewAdapter() {
        this.entrants = new HashMap<>();
    }

    public EntrantRecyclerViewAdapter(Map<String, String> entrants) {
        this.entrants = entrants;
    }

    public EntrantRecyclerViewAdapter(
            Map<String, String> entrants, boolean showRemoveButton, OnRemoveButtonClickListener listener) {
        this.entrants = entrants;
        this.showRemoveButton = showRemoveButton;
        this.removeButtonListener = listener;
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
    public void onBindViewHolder(@NonNull final EntrantViewHolder holder, int position) {
        // Only set the names if the entrant list is not empty
        if (entrants.isEmpty()) {
            return;
        }
        var binding = holder.binding;

        // Define the names
        String email = entrants.keySet().stream().collect(Collectors.toList()).get(position);
        String name = entrants.get(email);


        // Set the name of each person
        binding.entrantName.setText(name);

        // If we need to have the remove button, it is shown
        if (showRemoveButton) {
            binding.removeButton.setVisibility(View.VISIBLE);
        }

        // Sets Onclick listener for the remove button
        binding.removeButton.setOnClickListener(v -> removeButtonListener.onRemoveButtonClick(email));
    }

    @Override
    public int getItemCount() {
        return entrants.size();
    }
}
