package com.example.evently.ui.common;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.evently.R;
import com.example.evently.data.model.Event;

/**
 * Fragment base class that renders a live-updating list of {@link Event} objects.
 * <p>
 * Subclasses supply a {@link LiveData} source and refresh trigger, and provide
 * per-item click handling via {@link #onEventClick(Event)}.
 */
public abstract class LiveEventsFragment<T> extends Fragment {

    /**
     *
     * @return the {@link LiveData} source for events to render.
     */
    protected abstract LiveData<T> getLiveData();

    /**
     * Updates the list of events based on the latest observed data.
     *
     * @param target new value from {@link #getLiveData()}.
     * @param act callback to receive the updated {@link Event} list.
     */
    protected abstract void updateEventsBy(T target, Consumer<List<Event>> act);

    /**
     * Called when the fragment is ready to request a data refresh.
     */
    protected abstract void requestRefresh();

    /**
     * Handles clicks on an {@link Event} row.
     *
     * @param event clicked event.
     */
    protected abstract void onEventClick(Event event);

    /**
     * Override to provide a custom layout containing a RecyclerView.
     * @return the layout resource ID.
     */
    protected int getLayoutResId() {
        return R.layout.fragment_event_list;
    }

    /**
     * Override to locate a nested RecyclerView within a custom layout.
     * @param root the root view of the fragment.
     * @return the nested RecyclerView.
     */
    protected RecyclerView getRecyclerView(View root) {
        if (root instanceof RecyclerView recyclerView) {
            return recyclerView;
        }

        final var recyclerView = root.findViewById(R.id.event_list);
        if (recyclerView == null) {
            throw new AssertionError(
                    "LiveEventsFragment requires a RecyclerView with id event_list");
        }

        return (RecyclerView) recyclerView;
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        final var root = inflater.inflate(getLayoutResId(), container, false);
        final var recyclerView = getRecyclerView(root);

        Context context = recyclerView.getContext();
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(
                new EventRecyclerViewAdapter(new ArrayList<>(), this::onEventClick));

        getLiveData().observe(getViewLifecycleOwner(), target -> {
            updateEventsBy(target, events -> {
                final var adapter = new EventRecyclerViewAdapter(events, this::onEventClick);
                recyclerView.swapAdapter(adapter, false);
            });
        });

        requestRefresh();

        return root;
    }
}
