package com.example.evently.ui.common;

import java.text.MessageFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.evently.data.model.Event;
import com.example.evently.data.model.EventStatus;
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

    private static final DateTimeFormatter some_date =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("UTC"));
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
        var binding = holder.binding;

        // Title / name
        binding.content.setText(holder.mItem.name());

        // Poster
        binding.imgPoster.setImageResource(android.R.drawable.ic_menu_report_image);

        // Status + selectionDate
        EventStatus status = holder.mItem.computeStatus(Instant.now());

        switch (status) {
            case OPEN -> {
                binding.txtStatus.setText("Open");
                binding.txtselectionDate.setText(MessageFormat.format(
                        "Selection on {0}",
                        some_date.format(holder.mItem.selectionTime().toInstant())));
            }
            case CLOSED -> {
                binding.txtStatus.setText("Closed");
                binding.txtselectionDate.setText("Waitlist closed");
            }
        }
        binding.txtselectionDate.setVisibility(android.view.View.VISIBLE);

        // Event date
        binding.txtDate.setText(some_date.format(holder.mItem.eventTime().toInstant()));

        // Details button with no click logic
        binding.btnDetails.setOnClickListener(null);
        binding.btnDetails.setClickable(false);
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        public final FragmentEventBinding binding;
        public Event mItem;

        public EventViewHolder(FragmentEventBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
