package com.example.evently.ui.common;

import java.util.List;

import android.view.LayoutInflater;
import android.view.View;
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

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new EventViewHolder(
                FragmentEventBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(final EventViewHolder holder, int position) {
        // Attach the Event to the view.
        holder.mItem = mValues.get(position);


        // Title / name
        holder.binding.content.setText(holder.mItem.name());

        // The rest are placeholder bindings until Data Base integration
        // Poster
        holder.binding.imgPoster.setImageResource(android.R.drawable.ic_menu_report_image);

        // Status + selectionDate
        String[] statuses = new String[] {"Confirmed", "Open", "Closed"};
        String status = statuses[position % statuses.length];
        holder.binding.txtStatus.setText(status);

        if ("Open".equals(status)) {
            holder.binding.txtStatusSub.setVisibility(View.VISIBLE);
            holder.binding.txtStatusSub.setText("• Selection on 2025-12-" + (10 + (position % 9)));
        } else if ("Closed".equals(status)) {
            holder.binding.txtStatusSub.setVisibility(View.VISIBLE);
            holder.binding.txtStatusSub.setText("• Waitlist closed");
        } else {
            holder.binding.txtStatusSub.setVisibility(View.GONE);
        }

        // Event date
        holder.binding.txtDate.setText("2026-03-" + String.format("%02d", 1 + (position % 28)));

        // Details button with no click logic
        holder.binding.btnDetails.setOnClickListener(null);
        holder.binding.btnDetails.setClickable(false);
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        public final FragmentEventBinding binding;
        public final TextView mContentView;
        public Event mItem;

        public EventViewHolder(FragmentEventBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            this.mContentView = binding.content; // title text
        }

        @NonNull
        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}