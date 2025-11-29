package com.example.evently.ui.common;

import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.evently.R;
import com.example.evently.data.model.EventEntrants;
import com.example.evently.ui.model.EventViewModel;

/**
 * A reusable abstract fragment representing a list of entrants.
 * This is meant to serve as the template for all the "entrant list"s,
 * e.g: Enrolled entrants, Selected entrants, Cancelled entrants etc.
 * <p>
 * Extending classes can provide initial list of events by implementing `initEntrants`.
 * Extending classes will also have access to the {@link EntrantRecyclerViewAdapter} to modify dynamically.
 * @see EventRecyclerViewAdapter
 */
public abstract sealed class EntrantsFragment extends Fragment
        permits EntrantsFragment.EnrolledEntrantsFragment,
                EntrantsFragment.SelectedEntrantsFragment,
                EntrantsFragment.AcceptedEntrantsFragment,
                EntrantsFragment.CancelledEntrantsFragment {

    protected EventViewModel eventViewModel;

    /**
     * Select the type of entrants we aim to display.
     * <p>
     * The implementation of this function determines which list will be shown by this fragment.
     * @param entrantsInfo Lists of different types of entrants (all, selected, accepted, cancelled)
     * @return List of emails of all the entrants under a particular type.
     */
    protected abstract List<String> selectEntrantList(EventEntrants entrantsInfo);

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_entrant_list, container, false);

        eventViewModel = new ViewModelProvider(requireParentFragment()).get(EventViewModel.class);

        if (view instanceof RecyclerView recyclerView) {
            Context context = recyclerView.getContext();
            recyclerView.setLayoutManager(new LinearLayoutManager(context));

            // We set up an initial empty adapter, to allow the usage of swapAdapter later.
            recyclerView.setAdapter(new EntrantRecyclerViewAdapter());

            // Set to update the event entrants as they change.
            eventViewModel
                    .getEventEntrantsLive()
                    .observe(getViewLifecycleOwner(), eventEntrants -> {
                        final var adapter = getAdapter(eventEntrants);
                        recyclerView.swapAdapter(adapter, false);
                    });

            return view;
        } else {
            throw new AssertionError("EntrantsFragment.onCreateView called with non RecyclerView");
        }
    }

    /**
     * Gets the EntrantRecyclerViewAdapter for the entrants
     * @param entrants The entrants being adapted
     * @return An entrant adapter.
     */
    protected EntrantRecyclerViewAdapter getAdapter(EventEntrants entrants) {
        return new EntrantRecyclerViewAdapter(selectEntrantList(entrants));
    }

    public static final class EnrolledEntrantsFragment extends EntrantsFragment {
        @Override
        protected List<String> selectEntrantList(EventEntrants entrantsInfo) {
            return entrantsInfo.all();
        }
    }

    public static final class SelectedEntrantsFragment extends EntrantsFragment {

        /**
         * SelectedEntrantsFragment should have the remove button.
         * @param entrants The entrants being adapted
         * @return an entrant recycler view adapter
         */
        @Override
        protected EntrantRecyclerViewAdapter getAdapter(EventEntrants entrants) {
            return new EntrantRecyclerViewAdapter(
                    selectEntrantList(entrants), true, eventViewModel.eventID);
        }

        @Override
        protected List<String> selectEntrantList(EventEntrants entrantsInfo) {
            return entrantsInfo.selected();
        }
    }

    public static final class AcceptedEntrantsFragment extends EntrantsFragment {

        @Override
        protected List<String> selectEntrantList(EventEntrants entrantsInfo) {
            return entrantsInfo.accepted();
        }
    }

    public static final class CancelledEntrantsFragment extends EntrantsFragment {

        @Override
        protected List<String> selectEntrantList(EventEntrants entrantsInfo) {
            return entrantsInfo.cancelled();
        }
    }
}
