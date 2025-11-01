package com.example.evently.ui.common;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.OptionalInt;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.evently.R;
import com.example.evently.data.model.Notification;
import com.example.evently.databinding.FragmentNotifBinding;
import com.example.evently.utils.FirebaseAuthUtils;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Notification}.
 * <p>
 * This is meant to be a reusable class for displaying each {@link Notification}.
 * <p>
 * This should essentially be managed entirely by the owner fragment.
 */
public class NotificationRecyclerViewAdapter
        extends RecyclerView.Adapter<NotificationRecyclerViewAdapter.NotificationViewHolder> {

    public interface NotificationOnClickListener {
        void accept(Notification n);
    }

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("UTC"));
    private final List<Notification> mValues;
    private final NotificationOnClickListener onNotificationClick;
    private final String entrantEmail;

    public NotificationRecyclerViewAdapter(
            List<Notification> items, NotificationOnClickListener onNotificationClick) {
        mValues = items;
        entrantEmail = FirebaseAuthUtils.getCurrentEmail();
        this.onNotificationClick = onNotificationClick;
    }

    @NonNull @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NotificationViewHolder(FragmentNotifBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(final NotificationViewHolder holder, int position) {
        // Attach the Event to the view.
        holder.mItem = mValues.get(position);
        var notif = holder.mItem;
        var binding = holder.binding;
        var ctx = binding.getRoot().getContext();

        binding.notifTitle.setText(notif.title());
        binding.notifDescription.setText(notif.description());
        binding.notifDate.setText(DATE_TIME_FORMATTER.format(notif.creationTime()));

        // Highlight if not seen.
        if (!notif.hasSeen(entrantEmail)) {
            binding.notifCardLayout.setBackgroundColor(
                    ContextCompat.getColor(ctx, R.color.notif_highlight));
        }

        // Set the user provided listener.
        binding.notifCardLayout.setOnClickListener(v -> onNotificationClick.accept(notif));
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public OptionalInt findItemPosition(Predicate<Notification> predicate) {
        return IntStream.range(0, mValues.size())
                .filter(i -> predicate.test(mValues.get(i)))
                .findFirst();
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        public final FragmentNotifBinding binding;
        public Notification mItem;

        public NotificationViewHolder(FragmentNotifBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
