package com.example.evently.ui.common;

import java.text.MessageFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import android.view.LayoutInflater;
import android.view.View;
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

    public interface EventOnClickListener {
        void accept(Event n);
    }

    private static final DateTimeFormatter some_date =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("UTC"));
    private final List<Event> mValues;
    private final EventOnClickListener onEventClick;
    private List<Event> hiddenEvents = new ArrayList<>();

    /**
     * Creates an adapter for rendering {@link Event} items and handling per-item clicks.
     * @param items list of events to display; retained by reference.
     * @param onEventClick callback invoked when the item's “Details” button is pressed.
     */
    public EventRecyclerViewAdapter(List<Event> items, EventOnClickListener onEventClick) {
        mValues = items;
        this.onEventClick = onEventClick;
    }

    /**
     * Inflates the event row layout and creates a new {@link EventViewHolder}.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return a freshly constructed {@link EventViewHolder} wrapping the inflated binding.
     */
    @NonNull @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new EventViewHolder(FragmentEventBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(final EventViewHolder holder, int position) {
        // Attach the Event to the view.
        holder.mItem = mValues.get(position);
        var binding = holder.binding;

        // Filtering start
        if (hiddenEvents.contains(holder.mItem)) {
            holder.itemView.setVisibility(View.GONE);
            holder.itemView.getLayoutParams().height = 0;
        } else {
            holder.itemView.setVisibility(View.VISIBLE);
            holder.itemView.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
        }
        // Filtering end

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

        // Details button with given click logic.
        binding.btnDetails.setOnClickListener(v -> onEventClick.accept(holder.mItem));
    }

    /**
     * Returns the number of {@link Event} items currently held by the adapter.
     * @return the total item count to be rendered.
     */
    @Override
    public int getItemCount() {
        return mValues.size();
    }

    /**
     * ViewHolder for a single Event row.
     */
    public static class EventViewHolder extends RecyclerView.ViewHolder {
        public final FragmentEventBinding binding;
        public Event mItem;

        /**
         * Constructs a new ViewHolder using the provided ViewBinding.
         * The binding's root view is passed to the RecyclerView base class.
         * @param binding the binding for the row layout inflated from {@code fragment_event.xml}.
         */
        public EventViewHolder(FragmentEventBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public void updateEvents(List<Event> events) {
        hiddenEvents.clear();
        for (Event event : mValues) {
            if (!events.contains(event)) {
                hiddenEvents.add(event);
            }
        }
        notifyDataSetChanged();
    }
}
