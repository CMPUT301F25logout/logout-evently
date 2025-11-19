package com.example.evently.ui.entrant;

import static com.example.evently.ui.common.EntrantsFragment.EnrolledEntrantsFragment;

import com.example.evently.ui.common.EventDetailsFragment;

public class ViewEventDetailsFragment
        extends EventDetailsFragment<EnrolledEntrantsFragment, EntrantEventActionsFragment> {
    @Override
    protected Class<EnrolledEntrantsFragment> getFragmentForEntrantListContainer() {
        return EnrolledEntrantsFragment.class;
    }

    @Override
    protected Class<EntrantEventActionsFragment> getFragmentForActionButtonsContainer() {
        return EntrantEventActionsFragment.class;
    }
}
