package com.example.evently.ui.organizer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.fragment.NavHostFragment;

import com.example.evently.R;
import com.example.evently.data.EventsDB;
import com.example.evently.data.model.Event;
import com.example.evently.ui.common.EventsFragment;
import com.example.evently.utils.FirebaseAuthUtils;

public class OwnEventsFragment extends EventsFragment {

    // Local, mutable list backing the adapter
    private final ArrayList<Event> events = new ArrayList<>();

    @Override
    protected void onEventClick(Event event) {}

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_organizer_event_list;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button create = view.findViewById(R.id.btnCreateEvent);
        if (create != null) {
            create.setOnClickListener(v ->
                    NavHostFragment.findNavController(this).navigate(R.id.navigate_create_events));
        }

        // Receive result from CreateEventFragment (no ViewModel)
        var nav = NavHostFragment.findNavController(this);
        nav.getCurrentBackStackEntry()
                .getSavedStateHandle()
                .<Event>getLiveData("new_event")
                .observe(getViewLifecycleOwner(), event -> {
                    if (event != null) {
                        new EventsDB().storeEvent(event);
                        events.add(event);
                        if (adapter != null) {
                            adapter.notifyItemInserted(events.size() - 1);
                        }
                    }
                });
    }

    @Override
    protected void initEvents(Consumer<List<Event>> callback) {
        // TODO (chase): Get list of own events by organizer.
        if (events.isEmpty()) {
            new EventsDB()
                    .fetchEventsByOrganizers(FirebaseAuthUtils.getCurrentEmail(), callback, e -> {
                        Log.e("OwnEvents", e.toString());
                        Toast.makeText(
                                        requireContext(),
                                        "Something went wrong...",
                                        Toast.LENGTH_SHORT)
                                .show();
                    });
        }
        callback.accept(events);
    }
}
