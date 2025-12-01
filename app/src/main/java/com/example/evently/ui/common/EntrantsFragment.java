package com.example.evently.ui.common;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import com.example.evently.data.AccountDB;
import com.example.evently.data.EventsDB;
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
    protected boolean showRemoveButton = false;

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

        if (!(view instanceof RecyclerView recyclerView))
            throw new AssertionError("EntrantsFragment.onCreateView called with non RecyclerView");

        Context context = recyclerView.getContext();
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        // We set up an initial empty adapter, to allow the usage of swapAdapter later.
        recyclerView.setAdapter(new EntrantRecyclerViewAdapter());

        // Set up an observer to update the event entrants as they change.
        eventViewModel.getEventEntrantsLive().observe(getViewLifecycleOwner(), eventEntrants -> {
            final var selectedEntrantList = selectEntrantList(eventEntrants);
            new AccountDB().fetchAccounts(selectedEntrantList).thenRun(accounts -> {
                final var accountNames = accounts.stream()
                        .map(acc -> Map.entry(acc.email(), acc.name()))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                final var adapter = new EntrantRecyclerViewAdapter(
                        accountNames, showRemoveButton, this::cancelEntrant);
                recyclerView.swapAdapter(adapter, false);
            });
        });

        return view;
    }

    public static final class EnrolledEntrantsFragment extends EntrantsFragment {
        @Override
        protected List<String> selectEntrantList(EventEntrants entrantsInfo) {
            return entrantsInfo.all();
        }
    }

    public static final class SelectedEntrantsFragment extends EntrantsFragment {

        // Overrides the onCreateView to show the remove button.
        @Override
        public View onCreateView(
                LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            showRemoveButton = true;
            return super.onCreateView(inflater, container, savedInstanceState);
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

    /**
     * Cancel a selected entrant.
     * @param email The target entrant
     */
    private void cancelEntrant(String email) {
        EventsDB eventsDB = new EventsDB();

        assert eventViewModel.eventID != null;
        eventsDB.cancelSelectedUser(eventViewModel.eventID, email)
                .thenRun(x -> eventViewModel.requestEntrantsUpdate());
    }
}
