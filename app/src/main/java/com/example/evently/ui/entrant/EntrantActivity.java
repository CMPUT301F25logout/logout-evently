package com.example.evently.ui.entrant;

import java.util.UUID;

import android.os.Bundle;

import com.example.evently.R;
import com.example.evently.ui.common.ArchitectureActivity;
import com.example.evently.utils.IntentConstants;

/**
 * Activity entry point for the Entrant role.
 * This Activity hosts the Entrant navigation graph and UI.
 */
public class EntrantActivity extends ArchitectureActivity {

    /**
     * Get the navigation graph for this activity.
     *
     * @return {@code R.navigation.entrant_graph} the Entrant nav graph resource id.
     */
    @Override
    protected int getGraph() {
        return R.navigation.entrant_graph;
    }

    /**
     * Initializes the Entrant Activity and handles intent-driven navigation.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // We need to check if the intent had any extras signalling further navigation.
        var intent = getIntent();
        if (intent.hasExtra(IntentConstants.NOTIFICATION_INTENT_ID_KEY)) {
            // We were sent here by clicking on a notification.
            // Navigate to the notification page and let it handle the intent.
            navController.navigate(R.id.nav_notifs, null);
        }
        // Otherwise, need to check if the intent had URI data (i.e scanned QR code).
        var intentData = intent.getData();
        if (intentData != null) {
            // If we got here by scanning a QR code for an event, redirect to said event details
            // page.
            var eventID = intentData.getQueryParameter(IntentConstants.QR_EVENT_INTENT_ID_KEY);
            if (eventID != null) {
                final var action = HomeFragmentDirections.actionNavHomeToEventDetails(
                        UUID.fromString(eventID));
                navController.navigate(action);
            }
        }
    }
}
