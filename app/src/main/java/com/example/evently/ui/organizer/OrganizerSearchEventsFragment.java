package com.example.evently.ui.organizer;

import com.example.evently.ui.common.SearchEventsFragment;

/**
 * Organizer version of SearchEventsFragment
 */
public class OrganizerSearchEventsFragment
        extends SearchEventsFragment<OrganizerSearchedEventsFragment> {
    @Override
    protected Class<OrganizerSearchedEventsFragment> getSearchedEventsFragment() {
        return OrganizerSearchedEventsFragment.class;
    }
}
