package com.example.evently.ui.common;

import java.text.MessageFormat;
import java.util.List;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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
public class EventRecyclerViewAdapter extends RecyclerView.Adapter<EventRecyclerViewAdapter.EventViewHolder> {

    private static final DateTimeFormatter some_date = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("UTC"));
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
        // Title / name
        holder.binding.content.setText(holder.mItem.name());

        // Poster
        holder.binding.imgPoster.setImageResource(android.R.drawable.ic_menu_report_image);

        // Status + selectionDate
        String[] statuses = new String[] {"Confirmed", "Open", "Closed"};
        String status = statuses[position % statuses.length];
        holder.binding.txtStatus.setText(status);

        if ("Open".equals(status)) {
            holder.binding.txtselectionDate.setVisibility(android.view.View.VISIBLE);
            holder.binding.txtselectionDate.setText(MessageFormat.format("Selection on {0}", some_date.format(holder.mItem.selectionTime())));

        } else if ("Closed".equals(status)) {
            holder.binding.txtselectionDate.setVisibility(android.view.View.VISIBLE);
            holder.binding.txtselectionDate.setText("Waitlist closed");
        } else {
            holder.binding.txtselectionDate.setVisibility(android.view.View.GONE);
        }

        // Event date
        holder.binding.txtDate.setText(some_date.format(holder.mItem.eventTime()));

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

        @NonNull @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}