package com.example.evently.ui.entrant;

import com.example.evently.ui.common.SearchEventsFragment;

/**
 * Entrant version of SearchEventsFragment
 */
public class EntrantSearchEventsFragment
        extends SearchEventsFragment<EntrantSearchedEventsFragment> {
    @Override
    protected Class<EntrantSearchedEventsFragment> getSearchedEventsFragment() {
        return EntrantSearchedEventsFragment.class;
    }
}
