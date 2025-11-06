package com.example.evently.ui.entrant;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.evently.data.EventsDB;
import com.google.firebase.Timestamp;

import com.example.evently.R;
import com.example.evently.data.model.Category;
import com.example.evently.data.model.Event;
import com.example.evently.ui.common.EventsFragment;

public class BrowseEventsFragment extends EventsFragment {

    @Override
    protected void onEventClick(Event event) {
        // TODO (chase): Navigate to the event details fragment and attach the event ID argument!
        var action = BrowseEventsFragmentDirections.actionNavHomeToEventDetails(event.eventID().toString());
        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(action);
    }

    @Override
    protected int getLayoutRes() {
        // Layout with the two buttons + list
        return R.layout.fragment_event_entrants_list;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btnJoined = view.findViewById(R.id.btnJoined);
        Button btnBrowse = view.findViewById(R.id.btnBrowse);

        styleSelected(btnJoined, false);
        styleSelected(btnBrowse, true);

        // Navigate to Joined via action id
        btnJoined.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_browse_to_joined));

        // Already on Browse
        btnBrowse.setOnClickListener(v -> {});
    }

    private void styleSelected(Button b, boolean selected) {
        if (selected) {
            b.setBackgroundResource(R.drawable.bg_tab_selected);
            b.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));
        } else {
            b.setBackgroundResource(R.drawable.bg_tab_unselected);
            b.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black));
        }
    }

    @Override
    protected void initEvents(Consumer<List<Event>> callback) {
        new EventsDB().fetchEventsByDate(Timestamp.now(), callback, e -> {
            Log.e("BrowseEvents", e.toString());
            Toast.makeText(requireContext(), "Something went wrong...", Toast.LENGTH_SHORT).show();
        }, true);
    }
}
