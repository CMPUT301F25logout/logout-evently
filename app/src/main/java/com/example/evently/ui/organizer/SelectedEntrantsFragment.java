package com.example.evently.ui.organizer;

import com.example.evently.data.EventsDB;
import com.example.evently.ui.common.EntrantsFragment;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class SelectedEntrantsFragment extends EntrantsFragment {

    @Override
    protected void initEntrants(UUID eventID, Consumer<List<String>> callback) {
        new EventsDB()
                .fetchEventEntrants(List.of(eventID))
                .thenRun(entrantsInfo -> callback.accept(entrantsInfo.get(0).selected()));
    }
}