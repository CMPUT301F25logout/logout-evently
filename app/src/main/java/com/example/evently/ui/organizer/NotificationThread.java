package com.example.evently.ui.organizer;

import java.util.UUID;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.DialogFragment;

import com.example.evently.R;
import com.example.evently.data.NotificationDB;
import com.example.evently.data.model.Notification;

public class NotificationThread extends DialogFragment {

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Gets the eventID from the fragment.
        Bundle bundle = getArguments();
        assert bundle != null;
        UUID eventID = (UUID) bundle.getSerializable("eventID");
        Notification.Channel channel = (Notification.Channel) bundle.getSerializable("channel");


        // Fetches the event
        // TODO: Make the layout
        // TODO: Store past event notifications in a recycler view
        assert eventID != null;
        new NotificationDB().fetchEventNotifications(eventID);

        // TODO: Setup channel selection for notifications
        // TODO: Setup user input for the notification text
        // TODO: Show event name at the top of the fragment

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_notification_thread, container, false);
    }
}
