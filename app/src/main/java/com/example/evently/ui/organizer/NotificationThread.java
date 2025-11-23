package com.example.evently.ui.organizer;

import java.util.UUID;

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

public class NotificationThread extends DialogFragment {

    private UUID eventID;
    private Notification.Channel channel;
    private TextView eventTitle;
    private TextView eventChannel;
    private EditText titleText;
    private EditText descriptionText;
    private MaterialButton sendNotificationButton;

    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflates the view, and setups up the local variables
        View view = inflater.inflate(R.layout.fragment_notification_thread, container, false);
        setupVariables(view);

        // Sets the event title, and channel in the thread
        new EventsDB().fetchEvent(eventID).thenRun(event -> {
            if (event.isPresent()) {
                eventTitle.setText(event.get().name());
            } else {
                eventTitle.setText("Event not found!");
            }
        });
        eventChannel.setText(channel.toString());

        // Sets up the button to send a notification.

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
                    .storeNotification(new Notification(
                            UUID.randomUUID(), eventID, channel, title, description));
            toast("Notification sent!");
        });

        // TODO: Create some sort of notification channel view fragment, which can show the fetched
        new NotificationDB().fetchEventNotifications(eventID, channel);
        return view;
    }

    /**
     * Defines the variables used for a notification thread.
     * @param view The view with the items, like TextViews
     */
    private void setupVariables(View view) {
        // Gets the eventID, and channel from the fragment.
        Bundle bundle = getArguments();
        assert bundle != null;
        eventID = (UUID) bundle.getSerializable("eventID");
        channel = (Notification.Channel) bundle.getSerializable("channel");

        eventTitle = view.findViewById(R.id.tvEventTitle);
        eventChannel = view.findViewById(R.id.tvNotificationChannel);
        titleText = view.findViewById(R.id.etTitle);
        descriptionText = view.findViewById(R.id.etDescription);
        sendNotificationButton = view.findViewById(R.id.btnSendNotification);
    }

    /**
     * Shows a short-length {@link Toast} with the given message in this Fragment's context
     * @param msg message to display to the user
     */
    private void toast(String msg) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
    }
}
