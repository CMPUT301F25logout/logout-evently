package com.example.evently.ui.common;

import java.util.ArrayList;
import java.util.List;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.evently.data.AccountDB;
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

    private final List<String> entrants;
    private boolean showRemoveButton = false;
    private OnRemoveButtonClickListener removeButtonListener = ignored -> {};

    public EntrantRecyclerViewAdapter() {
        this.entrants = new ArrayList<>();
    }

    public EntrantRecyclerViewAdapter(List<String> entrants) {
        this.entrants = entrants;
    }

    public EntrantRecyclerViewAdapter(
            List<String> entrants, boolean showRemoveButton, OnRemoveButtonClickListener listener) {
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
        String name = entrants.get(position);

        // Set the name of each person
        new AccountDB().fetchAccount(name).optionally(account ->
                binding.entrantName.setText(account.name()));

        // If we need to have the remove button, it is shown
        if (showRemoveButton) {
            binding.removeButton.setVisibility(View.VISIBLE);
        }

        // Sets Onclick listener for the remove button
        binding.removeButton.setOnClickListener(v -> {

            // The following line of code is from Google, Gemini 3 Pro
            // "I have a recycler view adapter, and I am removing items from the recycler view
            // when a button within each recycler view item is clicked.
            //
            // I am currently trying to fix the remove button from my binding. The first clicked
            // item is deleting, but then when another item is clicked, it is removing an item
            // other than the one I clicked. How to fix this?", 2025-11-30
            int current_pos = holder.getBindingAdapterPosition();
            String email = entrants.get(current_pos);

            removeButtonListener.onRemoveButtonClick(email);
        });
    }

    @Override
    public int getItemCount() {
        return entrants.size();
    }
}
