package com.example.evently.ui.organizer;

import java.util.UUID;

import com.example.evently.ui.common.EventDetailsFragment;

public class EditEventDetailsFragment extends EventDetailsFragment<EventPeopleFragment> {
    private UUID eventID = null;

    @Override
    protected Class<EventPeopleFragment> getFragmentForEntrantListContainer() {
        return EventPeopleFragment.class;
    }

    @Override
    protected UUID getEventID() {
        if (eventID == null) {
            eventID = UUID.fromString(
                    EditEventDetailsFragmentArgs.fromBundle(getArguments()).getEventId());
        }
        return eventID;
    }

    // Organizer doesn't see the join/leave button.
    protected boolean shouldDisplayActionBtn() {
        return false;
    }
}
