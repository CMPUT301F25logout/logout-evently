package com.example.evently.ui.common;

import java.util.List;
import java.util.UUID;
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

/**
 * A reusable abstract fragment representing a list of entrants.
 * This is meant to serve as the template for all the "entrant list"s,
 * e.g: Enrolled entrants, Selected entrants, Cancelled entrants etc.
 * <p>
 * Extending classes can provide initial list of events by implementing `initEntrants`.
 * Extending classes will also have access to the {@link EntrantRecyclerViewAdapter} to modify dynamically.
 * @see EventRecyclerViewAdapter
 */
public abstract class EntrantsFragment extends Fragment {

    /**
     * Adapter to manage the entrant list dynamically.
     */
    protected EntrantRecyclerViewAdapter adapter;

    /**
     * This method will be called by onCreateView to set up the entrants view.
     * It is guaranteed that the activity context will be available at the time of calling.
     * @param callback Callback that will be passed the entrants into.
     */
    protected abstract void initEntrants(UUID eventID, Consumer<List<String>> callback);

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_entrant_list, container, false);

        final var args = getArguments();
        assert args != null;
        final var eventID = (UUID) args.getSerializable("eventID");

        if (view instanceof RecyclerView recyclerView) {
            Context context = recyclerView.getContext();
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            // Set up the recycler view adapter with the initial list of events (asynchronous).
            initEntrants(eventID, entrants -> {
                adapter = new EntrantRecyclerViewAdapter(entrants);
                recyclerView.setAdapter(adapter);
            });

            return view;
        } else {
            throw new AssertionError("EntrantsFragment.onCreateView called with non RecyclerView");
        }
    }
}
