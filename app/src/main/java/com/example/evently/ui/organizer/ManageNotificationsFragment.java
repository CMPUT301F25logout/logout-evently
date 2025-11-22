package com.example.evently.ui.organizer;

import java.util.List;
import java.util.function.Consumer;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.evently.data.model.Notification;
import com.example.evently.ui.common.NotificationsFragment;
import com.example.evently.utils.FirebaseAuthUtils;

public class ManageNotificationsFragment extends NotificationsFragment {

    protected void onNotificationClick(Notification notif) {

        NotificationThread thread = new NotificationThread();
        Bundle bundle = new Bundle();
        NotificationThread newThread = new NotificationThread();
        bundle.putSerializable("eventID", notif.eventId());
        bundle.putSerializable("channel", notif.channel());
        newThread.setArguments(bundle);

        // Shows the thread
        newThread.show(getChildFragmentManager(), "notificationThread");
    }

    protected void initNotifications(Consumer<List<Notification>> callback) {
        String email = FirebaseAuthUtils.getCurrentEmail();

        // Gets the organizer's notifications, and runs the callback function
        notificationDB.fetchNotificationsByOrganizer(email).thenRun(callback).catchE(e -> {
            Log.e("OrganizerNotificationFragment", e.toString());
            Toast.makeText(requireContext(), "Something went wrong...", Toast.LENGTH_SHORT)
                    .show();
        });
    }
}
