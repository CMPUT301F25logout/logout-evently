package com.example.evently.ui.admin;

import com.example.evently.ui.common.EventDetailsFragment;
import com.example.evently.ui.organizer.EventPeopleFragment;

/**
 * Admin view of the event details.
 * Uses {@link EventPeopleFragment} as one of the viewmodels.
 * Uses {@link AdminEventActionsFragment} as one of the viewmodels.
 * The event is removeable from this fragment using the {@link AdminEventActionsFragment}
 */
public class AdminEventDetailsFragment
        extends EventDetailsFragment<EventPeopleFragment, AdminEventActionsFragment> {

    @Override
    protected Class<EventPeopleFragment> getFragmentForEntrantListContainer() {
        return EventPeopleFragment.class;
    }

    @Override
    protected Class<AdminEventActionsFragment> getFragmentForActionButtonsContainer() {
        return AdminEventActionsFragment.class;
    }
}
