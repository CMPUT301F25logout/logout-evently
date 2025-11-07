package com.example.evently.ui.common;

import java.util.List;
import java.util.function.Consumer;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.evently.R;
import com.example.evently.data.NotificationDB;
import com.example.evently.data.model.Notification;

/**
 * A reusable abstract fragment representing a list of notifications.
 * This is meant to serve as the template for all the "browse notification" screens,
 * e.g: Browse received notifications, Browse sent notifications etc.
 * <p>
 * Extending classes can provide initial list of notifications by implementing `initNotifications`.
 * Extending classes will also have access to the {@link NotificationRecyclerViewAdapter} to modify dynamically.
 * @see NotificationRecyclerViewAdapter
 */
public abstract class NotificationsFragment extends Fragment {

    protected NotificationDB notificationDB;

    /**
     * Adapter to manage the notification list dynamically.
     */
    protected NotificationRecyclerViewAdapter adapter;

    protected RecyclerView recyclerView;

    /**
     * Listener to attach to the notification on click.
     * This may be different for the entrant vs organizer notification click.
     * @param notif The structural representation of the notification view that was clicked.
     */
    protected abstract void onNotificationClick(Notification notif);

    /**
     * This method will be called by onCreateView to set up the notifications view.
     * It is guaranteed that the activity context will be available at the time of calling.
     * @param callback Callback that will be passed the notifications into.
     */
    protected abstract void initNotifications(Consumer<List<Notification>> callback);

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifs, container, false);
        notificationDB = new NotificationDB();
        if (view instanceof RecyclerView recyclerView_) {
            this.recyclerView = recyclerView_;
            // Set the adapter
            Context context = recyclerView.getContext();
            recyclerView.setLayoutManager(new LinearLayoutManager(context));

            // Set up the recycler view adapter with the initial list of events (asynchronous).
            initNotifications(notifs -> {
                adapter = new NotificationRecyclerViewAdapter(notifs, this::onNotificationClick);
                recyclerView.setAdapter(adapter);
            });

            return view;
        } else {
            throw new AssertionError(
                    "NotificationsFragment.onCreateView called with non RecyclerView");
        }
    }
}
