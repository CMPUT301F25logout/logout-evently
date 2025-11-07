package com.example.evently.ui.common;

import java.util.List;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.evently.databinding.FragmentEventEntrantBinding;

/**
 * RecyclerView adapter that displays each entrant as a row with their profile picture and name.
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
            this.binding = binding;
        }
    }

    @NonNull @Override
    public EntrantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        FragmentEventEntrantBinding binding =
                FragmentEventEntrantBinding.inflate(inflater, parent, false);
        return new EntrantViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull EntrantViewHolder holder, int position) {
        String name = entrants.get(position);
        holder.binding.entrantName.setText(name);
        // Future: load profile picture, handle clicks, etc.
    }

    @Override
    public int getItemCount() {
        return entrants != null ? entrants.size() : 0;
    }
}
