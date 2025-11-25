package com.example.evently.ui.entrant;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.evently.data.model.Notification;
import com.example.evently.ui.common.NotificationsFragment;
import com.example.evently.utils.FirebaseAuthUtils;
import com.example.evently.utils.IntentConstants;

public class ViewNotificationsFragment extends NotificationsFragment {
    protected void onNotificationClick(Notification notif) {
        final var hasSeen = notif.seenBy().contains(FirebaseAuthUtils.getCurrentEmail());
        final var dialog =
                switch (notif.channel()) {
                    // Already accepted/declined - no need for the special accept/decline dialog.
                    case Winners ->
                        hasSeen ? new NotificationGenericDialog() : new NotificationWinnerDialog();
                    // TODO (chase): Do the other notifications need anything special or
                    //  is it okay for all of them to have the same dialog behavior (like here)?
                    default -> new NotificationGenericDialog();
                };
        var bundle = new Bundle();
        bundle.putSerializable("id", notif.id());
        bundle.putSerializable("eventID", notif.eventId());
        bundle.putString("title", notif.title());
        bundle.putString("message", notif.description());
        dialog.setArguments(bundle);
        dialog.show(getChildFragmentManager(), "EventNotification");
    }

    protected void initNotifications(Consumer<List<Notification>> callback) {
        String email = FirebaseAuthUtils.getCurrentEmail();
        notificationDB
                .fetchUserNotifications(email)
                .thenRun(notifs -> {
                    callback.accept(notifs);
                    handleNotificationClickIntent();
                })
                .catchE(e -> {
                    Log.e("ViewNotificationsFragment", e.toString());
                    Toast.makeText(requireContext(), "Something went wrong...", Toast.LENGTH_SHORT)
                            .show();
                });
    }

    /**
     * We may have been passed a notification ID to "blink" (double highlight).
     * This happens when a user clicks on a push notification.
     * This function checks if that's the case and handles it.
     *
     * @apiNote This function must only be used after the recyclerview and its adapter are populated.
     */
    private void handleNotificationClickIntent() {
        var intent = requireActivity().getIntent();
        if (intent.hasExtra(IntentConstants.NOTIFICATION_INTENT_ID_KEY)) {
            final UUID targetID = UUID.fromString(
                    intent.getStringExtra(IntentConstants.NOTIFICATION_INTENT_ID_KEY));

            // Find this notification in the adapter.
            // NOTE (chase): There is no guarantee that adapter is set by now. It waits on a network
            // call in onCreateView.
            // If we notice consistent exceptions here, we must switch to LiveData.
            // See: https://developer.android.com/topic/libraries/architecture/livedata#java
            var optionalPos = adapter.findItemPosition(notif -> notif.id().equals(targetID));
            // Scroll to this notification and highlight it.
            if (optionalPos.isPresent()) {
                var targetPos = optionalPos.getAsInt();
                recyclerView.smoothScrollToPosition(targetPos);
                // TODO (chase): Is there a way to simulate a "blink" on the notification?
            }
        }
    }
}
