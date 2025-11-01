package com.example.evently.ui.entrant;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.evently.data.model.Notification;
import com.example.evently.ui.common.NotificationsFragment;
import com.example.evently.utils.IntentConstants;

public class ViewNotificationsFragment extends NotificationsFragment {
    protected void onNotificationClick(Notification notif) {
        switch (notif.channel()) {
            case Winners -> {
                var dialog = new NotificationWinnerDialog();
                var bundle = new Bundle();
                bundle.putString("title", notif.title());
                bundle.putString("message", notif.description());
                dialog.setArguments(bundle);
                dialog.show(getChildFragmentManager(), "WinnerNotification");
            }
            default -> {
                // TODO (chase): Open respective dialogs.
            }
        }
    }

    protected void initNotifications(Consumer<List<Notification>> callback) {
        // TODO (chase): Obtain the real notifications from database.
        var notifs = new ArrayList<Notification>();
        var seenByOne = new HashSet<String>();
        seenByOne.add("rmaity@ualberta.ca");
        notifs.add(new Notification(
                UUID.fromString("91f1fda5-61ea-4d59-8dab-e6132285290c"),
                UUID.randomUUID(),
                Notification.Channel.Winners,
                "You have been invited to hiking",
                "Congratulations! You were chosen to participate in hiking by organizer1\nYou may accept or decline this invitation",
                Instant.now(),
                new HashSet<>()));
        notifs.add(new Notification(
                UUID.randomUUID(),
                UUID.randomUUID(),
                Notification.Channel.Losers,
                "You were not chosen for swimming",
                "Sorry! You were not chosen for this event at this time.",
                Instant.now(),
                seenByOne));
        callback.accept(notifs);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // We may have been passed a notification ID to "blink" (double highlight).
        var intent = requireActivity().getIntent();
        if (intent.hasExtra(IntentConstants.NOTIFICATION_INTENT_ID_KEY)) {
            final UUID targetID;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                targetID = intent.getSerializableExtra(
                        IntentConstants.NOTIFICATION_INTENT_ID_KEY, UUID.class);
            } else {
                targetID = (UUID)
                        intent.getSerializableExtra(IntentConstants.NOTIFICATION_INTENT_ID_KEY);
            }
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
