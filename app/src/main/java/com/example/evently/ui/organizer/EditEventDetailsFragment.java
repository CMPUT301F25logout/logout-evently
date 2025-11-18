package com.example.evently.ui.organizer;

import com.example.evently.ui.common.EventDetailsFragment;
import com.example.evently.ui.entrant.EntrantEventActionsFragment;

public class EditEventDetailsFragment
        extends EventDetailsFragment<EventPeopleFragment, EntrantEventActionsFragment> {

    @Override
    protected Class<EventPeopleFragment> getFragmentForEntrantListContainer() {
        return EventPeopleFragment.class;
    }

    @Override
    protected Class<EntrantEventActionsFragment> getFragmentForActionButtonsContainer() {
        return EntrantEventActionsFragment.class;
    }
}
