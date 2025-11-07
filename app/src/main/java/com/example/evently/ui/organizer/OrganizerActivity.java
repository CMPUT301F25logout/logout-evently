package com.example.evently.ui.organizer;

import com.example.evently.R;
import com.example.evently.ui.common.ArchitectureActivity;

/**
 * Activity for the organizer roler which hosts the organizer specific navigation graph
 */
public class OrganizerActivity extends ArchitectureActivity {

    /**
     * Returns the navigation graph used for organizer flows
     *
     * @return the resource ID of {@code R.navigation.organizer_graph}.
     */
    @Override
    protected int getGraph() {
        return R.navigation.organizer_graph;
    }
}
