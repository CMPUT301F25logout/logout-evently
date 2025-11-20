package com.example.evently.ui.admin;

import android.util.Log;
import android.widget.Toast;

import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.evently.data.EventsDB;
import com.example.evently.data.model.Event;
import com.example.evently.ui.common.EventsFragment;
import com.example.evently.ui.organizer.HomeFragmentDirections;

import java.util.List;
import java.util.function.Consumer;

public class AdminEventsFragment extends EventsFragment
{

	@Override
	protected void onEventClick(Event event) {
		var action = HomeFragmentDirections.actionNavHomeToEventDetails(event.eventID());
		NavController navController = NavHostFragment.findNavController(this);
		navController.navigate(action);
	}

	@Override
	protected void initEvents(Consumer<List<Event>> callback) {

		new EventsDB()
				.fetchAllEvents()
				.thenRun(callback)
				.catchE(e -> {
					Log.e("Admin Events", e.toString());
					Toast.makeText(requireContext(), "Something went wrong...", Toast.LENGTH_SHORT)
							.show();
				});
	}
}
