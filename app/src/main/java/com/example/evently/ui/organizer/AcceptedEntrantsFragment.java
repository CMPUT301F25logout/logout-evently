package com.example.evently.ui.organizer;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import com.example.evently.data.EventsDB;
import com.example.evently.ui.common.EntrantsFragment;

public class AcceptedEntrantsFragment extends EntrantsFragment {

    @Override
    protected void initEntrants(UUID eventID, Consumer<List<String>> callback) {
        new EventsDB()
                .fetchEventEntrants(List.of(eventID))
                .thenRun(entrantsInfo -> callback.accept(entrantsInfo.get(0).accepted()));
    }
}
