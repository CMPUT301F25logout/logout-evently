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

import com.google.firebase.storage.StorageReference;

import com.example.evently.data.EventsDB;
import com.example.evently.data.model.Event;
import com.example.evently.data.model.EventStatus;
import com.example.evently.databinding.FragmentEventBinding;
import com.example.evently.utils.GlideUtils;

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

    private static final DateTimeFormatter SELECTION_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault());
    private static final DateTimeFormatter EVENT_DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());
    private final List<Event> mValues;
    private final EventOnClickListener onEventClick;

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

        // Title / name
        binding.content.setText(holder.mItem.name());

        // Gets a reference to the poster, and stores it in the image view.
        StorageReference posterReference =
                new EventsDB().getPosterStorageRef(holder.mItem.eventID());
        posterReference
                .getMetadata()
                .addOnSuccessListener(metadata -> {
                    // Event image exists, set image visible and display it
                    binding.imgPoster.setVisibility(android.view.View.VISIBLE);
                    GlideUtils.loadPosterIntoImageView(posterReference, binding.imgPoster);
                })
                .addOnFailureListener(e -> {
                    // Event image does NOT exist, do nothing
                    ;
                });

        // Status + selectionDate
        EventStatus status = holder.mItem.computeStatus(Instant.now());

        switch (status) {
            case OPEN -> {
                binding.txtStatus.setText("Open");
                binding.txtselectionDate.setText(MessageFormat.format(
                        "Selection date: {0}",
                        SELECTION_DATE_FORMATTER.format(
                                holder.mItem.selectionTime().toInstant())));
            }
            case CLOSED -> {
                binding.txtStatus.setText("Closed");
                binding.txtselectionDate.setText("Waitlist closed");
            }
        }

        // Event date
        binding.txtDate.setText(
                EVENT_DATE_TIME_FORMATTER.format(holder.mItem.eventTime().toInstant()));

        // Details button with given click logic.
        binding.btnDetails.setOnClickListener(v -> onEventClick.accept(holder.mItem));

        // Event description
        binding.txtDescription.setText(holder.mItem.description());

        // Event category
        binding.txtCategory.setText(holder.mItem.category().toString());

        // Entrants count
        EventsDB db = new EventsDB();
        db.fetchEventEntrants(holder.mItem.eventID()).thenRun(eventEntrants -> {
            Integer entrants = eventEntrants.get().all().size();
            if (holder.mItem.optionalEntrantLimit().isEmpty()) {
                binding.txtEntrants.setText(MessageFormat.format("{0} Entrants", entrants));
            } else {
                binding.txtEntrants.setText(MessageFormat.format(
                        "{0} / {1} Entrants",
                        entrants, holder.mItem.optionalEntrantLimit().get()));
            }
        });

        // Selection Limit
        binding.txtSelectionLimit.setText(
                MessageFormat.format("Seats: {0}", holder.mItem.selectionLimit()));
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
}
