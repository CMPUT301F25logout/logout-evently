package com.example.evently.ui.organizer;

import java.util.List;
import java.util.function.Consumer;

import android.util.Log;
import android.widget.Toast;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.evently.data.model.Notification;
import com.example.evently.data.model.Notification.Channel;
import com.example.evently.ui.common.NotificationsFragment;
import com.example.evently.utils.FirebaseAuthUtils;

public class ManageNotificationsFragment extends NotificationsFragment {

    protected void onNotificationClick(Notification notif) {

        Channel c = notif.channel();
        Log.d("notif click", "onNotificationClick: " + c);
        String channelName = c.toString();
        Log.d("notif click", "channelName: " + channelName);

        // Creates the safe args for the nav-thread
        var action = ManageNotificationsFragmentDirections.actionNavNotifsToNavThread(
                notif.eventId(), channelName);
        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(action);
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
