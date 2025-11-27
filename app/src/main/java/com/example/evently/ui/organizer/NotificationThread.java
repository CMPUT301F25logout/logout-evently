package com.example.evently.ui.organizer;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.button.MaterialButton;

import com.example.evently.R;
import com.example.evently.data.EventsDB;
import com.example.evently.data.NotificationDB;
import com.example.evently.data.model.Notification;
import com.example.evently.data.model.Notification.Channel;
import com.example.evently.ui.common.NotificationsFragment;

public class NotificationThread extends DialogFragment {

    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflates the view, and setups up the local variables
        View view = inflater.inflate(R.layout.fragment_notification_thread, container, false);
        // Gets the eventID, and channel from the fragment.
        Bundle bundle = getArguments();
        assert bundle != null;
        UUID eventID = (UUID) bundle.getSerializable("eventID");
        Channel channel = Channel.valueOf((String) bundle.getSerializable("channel"));

        // Gets the views
        TextView eventTitle = view.findViewById(R.id.tvEventTitle);
        TextView eventChannel = view.findViewById(R.id.tvNotificationChannel);
        EditText titleText = view.findViewById(R.id.etTitle);
        EditText descriptionText = view.findViewById(R.id.etDescription);
        MaterialButton sendNotificationButton = view.findViewById(R.id.btnSendNotification);

        // Creates the notificationThreadRecycler view, and passes it the necessary params
        ViewThreadNotifications threadNotifications = new ViewThreadNotifications();
        bundle = new Bundle();
        bundle.putSerializable("eventID", eventID);
        bundle.putSerializable("channel", channel);
        threadNotifications.setArguments(bundle);

        if (savedInstanceState == null) {
            // Load the notification threads into the container
            getChildFragmentManager()
                    .beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.notificationThreadContainer, threadNotifications, null)
                    .commit();
        }

        // Sets the event title, and channel in the thread
        new EventsDB().fetchEvent(eventID).thenRun(event -> {
            event.ifPresent(value -> eventTitle.setText(value.name()));
        });
        eventChannel.setText(channel.toString());

        // Sets up the button to send a notification when pressed.
        sendNotificationButton.setOnClickListener(v -> {
            String title = titleText.getText().toString().strip();
            String description = descriptionText.getText().toString().strip();

            if (title.isEmpty()) {
                toast("Error: Please enter a title");
                return;
            }
            if (description.isEmpty()) {
                toast("Error: Please enter a description");
                return;
            }

            // If notification is valid, it is sent!
            new NotificationDB()
                    .storeNotification(new Notification(eventID, channel, title, description));
            toast("Notification sent!");
        });
        return view;
    }

    /**
     * Shows a short-length {@link Toast} with the given message in this Fragment's context
     * @param msg message to display to the user
     */
    private void toast(String msg) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * The following class is for viewing a thread notification
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
