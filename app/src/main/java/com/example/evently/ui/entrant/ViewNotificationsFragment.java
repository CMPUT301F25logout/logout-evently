package com.example.evently.ui.entrant;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import android.os.Bundle;

import com.example.evently.data.model.Notification;
import com.example.evently.ui.common.NotificationsFragment;

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
                UUID.randomUUID(),
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
}
