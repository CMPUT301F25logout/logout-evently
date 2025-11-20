package com.example.evently.ui.admin;

import android.util.Log;
import android.widget.Toast;

import com.example.evently.data.model.Notification;
import com.example.evently.ui.common.NotificationsFragment;

import java.util.List;
import java.util.function.Consumer;

/**
 * A fragment representing a list of notifications the admin can browse.
 */
public class AdminBrowseNotificationsFragment extends NotificationsFragment
{

	/**
	 * Handles clicks on a notification row in the Admin Browse list.
	 * Does nothing unless it needs to.
	 * @param notif The structural representation of the Event view that was clicked.
	 */
	@Override
	protected void onNotificationClick(Notification notif) {

	}

	/**
	 * Supplies the Browse list with all notifications.
	 * @param callback Callback that will be passed the notifications into.
	 */
	@Override
	protected void initNotifications(Consumer<List<Notification>> callback) {
		notificationDB.fetchAllNotifications().thenRun(callback).catchE(e ->
		{
			Log.e("AdminBrowseNotificationsFragment", e.toString());
			Toast.makeText(requireContext(), "Something went wrong...", Toast.LENGTH_SHORT)
					.show();
		});
	}
}
