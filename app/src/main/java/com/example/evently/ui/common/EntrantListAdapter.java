package com.example.evently.ui.common;

import java.util.List;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.evently.databinding.FragmentEventEntrantBinding;

/**
 * Recycler view that displays each entrant as a row with their profile picture and name
 * @Author Vinson Lou
 */
public class EntrantListAdapter extends RecyclerView.Adapter<EntrantListAdapter.EntrantViewHolder> {
    private final List<String> entrants;

    public EntrantListAdapter(List<String> entrants) {
        this.entrants = entrants;
    }

    public static class EntrantViewHolder extends RecyclerView.ViewHolder {
        public final FragmentEventEntrantBinding binding;

        public EntrantViewHolder(FragmentEventEntrantBinding binding) {
            super(binding.getRoot());
            // Can define click listeners here
            this.binding = binding;
        }
    }

    @NonNull @Override
    public EntrantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new EntrantViewHolder(FragmentEventEntrantBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(final EntrantViewHolder holder, int position) {
        var binding = holder.binding;
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
