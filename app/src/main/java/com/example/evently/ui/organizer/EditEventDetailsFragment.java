package com.example.evently.ui.organizer;

import com.example.evently.ui.common.EventDetailsFragment;

public class EditEventDetailsFragment
        extends EventDetailsFragment<EventPeopleFragment, OrganizerEventActionsFragment> {

    @Override
    protected Class<EventPeopleFragment> getFragmentForEntrantListContainer() {
        return EventPeopleFragment.class;
    }

    @Override
    protected Class<OrganizerEventActionsFragment> getFragmentForActionButtonsContainer() {
        return OrganizerEventActionsFragment.class;
    }
}
