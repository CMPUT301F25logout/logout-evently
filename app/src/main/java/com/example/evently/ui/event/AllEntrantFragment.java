package com.example.evently.ui.event;

import java.util.Collections;
import java.util.List;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.evently.R;
import com.example.evently.ui.common.EntrantListAdapter;

/**
 * Container class that holds the 3 fragments for the different people tabs.
 * Each tab shows a list of entrants.
 */
public class AllEntrantFragment {

    /**
     * Retrieves a list of entrants for a given category.
     * In production, this can be replaced by a database or ViewModel call.
     *
     * @param category Type of entrant list to fetch ("enrolled", "cancelled", "selected")
     * @return List of entrants for that category
     */
    protected static List<String> getEntrants(@NonNull String category) {
        // TODO: Replace with data like from Firestore
        return Collections.emptyList();
    }

    /** Utility function to set up RecyclerView with an EntrantListAdapter */
    private static void setupEntrantList(@NonNull View rootView, @NonNull List<String> entrants) {
        RecyclerView recyclerView = rootView.findViewById(R.id.entrantRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(rootView.getContext()));
        recyclerView.setAdapter(new EntrantListAdapter(entrants));
    }

    /**
     * Fragment to display list of all enrolled people in an event
     */
    public static class EnrolledPeopleFragment extends Fragment {
        @Nullable @Override
        public View onCreateView(
                @NonNull LayoutInflater inflater,
                @Nullable ViewGroup container,
                @Nullable Bundle savedInstanceState) {

            View view = inflater.inflate(R.layout.fragment_event_people_all_list, container, false);
            setupEntrantList(view, getEntrants("enrolled"));
            return view;
        }
    }

    /**
     * Fragment to display list of all people who cancelled in an event
     */
    public static class CancelledPeopleFragment extends Fragment {
        @Nullable @Override
        public View onCreateView(
                @NonNull LayoutInflater inflater,
                @Nullable ViewGroup container,
                @Nullable Bundle savedInstanceState) {

            View view = inflater.inflate(R.layout.fragment_event_people_all_list, container, false);
            setupEntrantList(view, getEntrants("cancelled"));
            return view;
        }
    }

    /**
     * Fragment to display list of all selected people in an event
     */
    public static class SelectedPeopleFragment extends Fragment {
        @Nullable @Override
        public View onCreateView(
                @NonNull LayoutInflater inflater,
                @Nullable ViewGroup container,
                @Nullable Bundle savedInstanceState) {

            View view = inflater.inflate(R.layout.fragment_event_people_all_list, container, false);
            setupEntrantList(view, getEntrants("selected"));
            return view;
        }
    }
}
