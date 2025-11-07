package com.example.evently.ui.entrant;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import com.example.evently.data.AccountDB;
import com.example.evently.data.EventsDB;
import com.example.evently.data.generic.PromiseOpt;
import com.example.evently.data.model.Account;
import com.example.evently.data.model.EventEntrants;
import com.example.evently.ui.common.EntrantsFragment;

public class EnrolledEntrantsFragment extends EntrantsFragment {

    @Override
    protected void initEntrants(UUID eventID, Consumer<List<String>> callback) {
        new EventsDB()
                .fetchEventEntrants(List.of(eventID))
                .thenRun(entrantsInfo -> callback.accept(entrantsInfo.get(0).all()));
    }
}
