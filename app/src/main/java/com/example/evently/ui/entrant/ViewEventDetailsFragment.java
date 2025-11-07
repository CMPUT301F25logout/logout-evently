package com.example.evently.ui.entrant;

import java.util.UUID;

import com.example.evently.ui.common.EventDetailsFragment;

public class ViewEventDetailsFragment extends EventDetailsFragment<EnrolledEntrantsFragment> {
    private UUID eventID = null;

    @Override
    protected Class<EnrolledEntrantsFragment> getFragmentForEntrantListContainer() {
        return EnrolledEntrantsFragment.class;
    }



    @Override
    protected UUID getEventID() {
        if (eventID == null) {
            eventID = UUID.fromString(
                    ViewEventDetailsFragmentArgs.fromBundle(getArguments()).getEventId());
        }
        return eventID;
    }
}
