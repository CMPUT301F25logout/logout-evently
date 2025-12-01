package com.example.evently.ui.admin;

import com.example.evently.ui.common.EventDetailsFragment;
import com.example.evently.ui.organizer.EventMetaFragment;

/**
 * Admin view of the event details.
 * Uses {@link EventMetaFragment} as one of the viewmodels.
 * Uses {@link AdminEventActionsFragment} as one of the viewmodels.
 * The event is removeable from this fragment using the {@link AdminEventActionsFragment}
 */
public class AdminEventDetailsFragment
        extends EventDetailsFragment<EventMetaFragment, AdminEventActionsFragment> {

    @Override
    protected Class<EventMetaFragment> getFragmentForEntrantListContainer() {
        return EventMetaFragment.class;
    }

    @Override
    protected Class<AdminEventActionsFragment> getFragmentForActionButtonsContainer() {
        return AdminEventActionsFragment.class;
    }
}
