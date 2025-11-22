package com.example.evently.ui.organizer;

import java.util.UUID;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.button.MaterialButton;

import com.example.evently.R;
import com.example.evently.data.NotificationDB;
import com.example.evently.data.model.Notification;

public class NotificationThread extends DialogFragment {

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Gets the eventID, and channel from the fragment.
        Bundle bundle = getArguments();
        assert bundle != null;
        UUID eventID = (UUID) bundle.getSerializable("eventID");
        Notification.Channel channel = (Notification.Channel) bundle.getSerializable("channel");

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_notification_thread, container, false);

        TextView eventTitle = view.findViewById(R.id.tvEventTitle);
        TextView eventChannel = view.findViewById(R.id.tvNotificationChannel);

        // TODO: Show the eventTitle, and eventChannel (eventTitle needs to be fetched)

        MaterialButton sendNotificationButton = view.findViewById(R.id.btnSendNotification);

        sendNotificationButton.setOnClickListener(v -> {
            // TODO: Figure out the material TextInputEditText for getting text

            // TODO: Input validation toasts
        });

        assert eventID != null;
        assert channel != null;
        new NotificationDB().fetchEventNotifications(eventID, channel);

        // TODO: Create some sort of notification channel view fragment, which can show the fetched
        // notifications for the channel.
        return view;
    }
}
