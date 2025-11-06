package com.example.evently.ui.entrant;

import java.util.List;
import java.util.function.Consumer;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.core.content.ContextCompat;
import androidx.navigation.fragment.NavHostFragment;

import com.example.evently.R;
import com.example.evently.data.EventsDB;
import com.example.evently.data.model.Event;
import com.example.evently.ui.common.EventsFragment;
import com.example.evently.utils.FirebaseAuthUtils;

public class JoinedEventsFragment extends EventsFragment {

    @Override
    protected void onEventClick(Event event) {}

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_event_entrants_list;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btnJoined = view.findViewById(R.id.btnJoined);
        Button btnBrowse = view.findViewById(R.id.btnBrowse);

        styleSelected(btnJoined, true);
        styleSelected(btnBrowse, false);

        // Navigate back to Browse via action id
        btnBrowse.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_joined_to_browse));

        // Already on Joined
        btnJoined.setOnClickListener(v -> {});
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
        new EventsDB().fetchEventsByEnrolled(FirebaseAuthUtils.getCurrentEmail(), callback, e -> {
            Log.e("JoinedEvents", e.toString());
            Toast.makeText(requireContext(), "Something went wrong...", Toast.LENGTH_SHORT)
                    .show();
        });
    }
}
