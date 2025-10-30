package com.example.evently.ui.common;

import java.util.List;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.evently.data.model.Event;
import com.example.evently.databinding.FragmentEventBinding;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Event}.
 * <p>
 * This is meant to be a reusable class for displaying each {@link Event}.
 * <p>
 * This is not abstract since there's no requirement for displaying {@link Event}s differently.
 * They always look the same (a little box with all the event brief info + picture).
 */
public class EventRecyclerViewAdapter
        extends RecyclerView.Adapter<EventRecyclerViewAdapter.EventViewHolder> {

    private final List<Event> mValues;

    public EventRecyclerViewAdapter(List<Event> items) {
        mValues = items;
    }

    @NonNull @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return new EventViewHolder(FragmentEventBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(final EventViewHolder holder, int position) {
        // Attach the Event to the view.
        holder.mItem = mValues.get(position);
        // TODO: Set text views and similar as per event representation.
        holder.mContentView.setText(holder.mItem.name());
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    // TODO: Incomplete.
    public static class EventViewHolder extends RecyclerView.ViewHolder {
        // TODO: Add other views in here that represent an event.
        public final TextView mContentView;
        public Event mItem;

        public EventViewHolder(FragmentEventBinding binding) {
            super(binding.getRoot());
            // TODO: Assign all the views here. Don't assign Event.
            mContentView = binding.content;
        }

        @NonNull @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
