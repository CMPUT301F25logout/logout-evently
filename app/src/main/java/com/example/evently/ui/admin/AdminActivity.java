package com.example.evently.ui.admin;

import com.example.evently.R;
import com.example.evently.ui.common.ArchitectureActivity;

/**
 * Activity entry point for the Admin role.
 * This Activity hosts the Admin navigation graph and UI.
 */
public class AdminActivity extends ArchitectureActivity {
    /**
     * Get the navigation graph for this activity.
     *
     * @return {@code R.navigation.admin_graph} the Admin nav graph resource id.
     */
    @Override
    protected int getGraph() {
        return R.navigation.admin_graph;
    }

    /**
     * Get the menu for the admin activity.
     * @return {@code R.menu.admin_navbar} the Admin nav bar resource id.
     */
    @Override
    protected int getMenu() {
        return R.menu.admin_navbar;
    }
}
