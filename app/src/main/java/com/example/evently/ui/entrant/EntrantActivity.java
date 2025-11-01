package com.example.evently.ui.entrant;

import android.os.Bundle;

import com.example.evently.R;
import com.example.evently.ui.common.ArchitectureActivity;
import com.example.evently.utils.IntentConstants;

import java.util.UUID;

public class EntrantActivity extends ArchitectureActivity {

    @Override
    protected int getGraph() {
        return R.navigation.entrant_graph;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // We need to check if the intent had any extras signalling further navigation.
        var intent = getIntent();
        if (intent.hasExtra(IntentConstants.NOTIFICATION_INTENT_ID_KEY)) {
            // We were sent here by clicking on a notification.
            // Navigate to the notification page and highlight the notification in question.
            UUID targetNotificationID = (UUID) intent.getSerializableExtra(IntentConstants.NOTIFICATION_INTENT_ID_KEY);
            var bundle = new Bundle();
            bundle.putSerializable(IntentConstants.NOTIFICATION_INTENT_ID_KEY, targetNotificationID);
            navController.navigate(R.id.nav_notifs, bundle);
        }
    }
}
