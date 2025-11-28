package com.example.evently.ui.organizer;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.evently.R;
import com.example.evently.data.EventsDB;
import com.example.evently.data.NotificationDB;
import com.example.evently.data.model.Notification;
import com.example.evently.data.model.Notification.Channel;
import com.example.evently.databinding.FragmentNotificationThreadBinding;
import com.example.evently.ui.common.NotificationsFragment;

/**
 * The following class is a notification thread, which displays the event title, and notification
 * channel. It also allows a user to see the previous notifications sent to the same channel for the
 * event, and to send more notifications to that channel.
 * @author alexander-b
 */
public class NotificationThread extends DialogFragment {

    private FragmentNotificationThreadBinding binding;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Gets the binding, and arguments
        binding = FragmentNotificationThreadBinding.inflate(inflater, container, false);
        NotificationThreadArgs args = NotificationThreadArgs.fromBundle(getArguments());

        // Gets the eventID, and channel from the fragment.
        UUID eventID = args.getEventID();
        Channel channel = Channel.valueOf(args.getChannel());

        // Sets the event title
        new EventsDB()
                .fetchEvent(eventID)
                .thenRun(event -> event.ifPresent(value ->
                        binding.tvEventTitle.setText(String.format("Event: %s", value.name()))));

        // Sets the channel
        String channelText = "Channel: " + channel.name();
        binding.tvNotificationChannel.setText(channelText);

        // Sets up the button to send a notification when pressed.
        binding.btnSendNotification.setOnClickListener(v -> {

            // Gets entered title and description
            String title = binding.etTitle.getText().toString().strip();
            String description = binding.etDescription.getText().toString().strip();

            // If title or description is missing, we do not create anything.
            String miss = title.isEmpty() ? "title" : description.isEmpty() ? "description" : null;
            if (miss != null) {
                toast("Error: Please enter a " + miss);
                return;
            }

            // If notification is valid, it is sent, and navigates to previous fragment.
            new NotificationDB()
                    .storeNotification(new Notification(eventID, channel, title, description))
                    .thenRun(x -> {
                        toast("Notification sent!");
                        NavHostFragment.findNavController(this).navigateUp();
                    });
        });

        // Returns the view
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Forwards arguments to the viewThreadNotifications fragment
        ViewThreadNotifications viewThreadNotifications = new ViewThreadNotifications();
        viewThreadNotifications.setArguments(getArguments());

        if (savedInstanceState == null) {
            getChildFragmentManager()
                    .beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.notificationThreadContainer, viewThreadNotifications, null)
                    .commit();
        }
    }

    /**
     * Shows a short-length {@link Toast} with the given message in this Fragment's context
     * @param msg message to display to the user
     */
    private void toast(String msg) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * The following class is for viewing the notifications to a specific event, and notification
     * channel
     * @author alexander-b
     */
    public static class ViewThreadNotifications extends NotificationsFragment {

        private UUID eventID;
        private Notification.Channel channel;

        // The notification does nothing when clicked
        protected void onNotificationClick(Notification n) {}

        @Override
        public View onCreateView(
                LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            // Gets the eventID, and channel
            final var args = NotificationThreadArgs.fromBundle(getArguments());

            eventID = args.getEventID();
            channel = Channel.valueOf(args.getChannel());
            return super.onCreateView(inflater, container, savedInstanceState);
        }

        /**
         * The following function initiates the list of notifications in a recycler view.
         * @param callback Callback that consumes a list of notifications
         */
        protected void initNotifications(Consumer<List<Notification>> callback) {
            // Runs the callback for initNotifications.
            notificationDB.fetchEventNotifications(eventID, channel).thenRun(callback);
        }
    }
}
