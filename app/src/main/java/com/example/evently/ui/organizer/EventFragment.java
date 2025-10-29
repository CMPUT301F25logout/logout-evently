package com.example.evently.ui.organizer;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.evently.R;

import java.util.ArrayList;

/**
 * A fragment representing a list of Events.
 */
public class EventFragment extends Fragment {
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public EventFragment() {}

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_list, container, false);

        if (view instanceof RecyclerView recyclerView) {
            // Set the adapter
            Context context = recyclerView.getContext();
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            // TODO: Obtain events and pass them here.
            recyclerView.setAdapter(new EventRecyclerViewAdapter(new ArrayList<>()));
        }

        return view;
    }
}
