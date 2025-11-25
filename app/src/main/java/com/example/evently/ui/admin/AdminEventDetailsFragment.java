package com.example.evently.ui.admin;

import android.widget.Toast;
import androidx.fragment.app.DialogFragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.evently.data.EventsDB;
import com.example.evently.ui.common.ConfirmDeleteDialog;
import com.example.evently.ui.common.EventDetailsFragment;
import com.example.evently.ui.organizer.EventPeopleFragment;

/**
 * Admin view of the event details.
 * Uses {@link EventPeopleFragment} as one of the viewmodels.
 * Uses {@link AdminEventActionsFragment} as one of the viewmodels.
 * Implements ConfirmDeleteListener for Confirm/Cancel callbacks
 */
public class AdminEventDetailsFragment
        extends EventDetailsFragment<EventPeopleFragment, AdminEventActionsFragment>
        implements ConfirmDeleteDialog.ConfirmDeleteListener {

    @Override
    protected Class<EventPeopleFragment> getFragmentForEntrantListContainer() {
        return EventPeopleFragment.class;
    }

    @Override
    protected Class<AdminEventActionsFragment> getFragmentForActionButtonsContainer() {
        return AdminEventActionsFragment.class;
    }

    /**
     * The dialog closed with a positive click (confirm).
     * Delete the event from the eventsDB and navigate back to the event list
     * @param dialog The dialog that was showed.
     */
    @Override
    public void onDialogConfirmClick(DialogFragment dialog) {
        // Delete event
        final EventsDB eventDB = new EventsDB();
        eventDB.deleteEvent(eventViewModel.eventID);

        Toast.makeText(requireContext(), "Event was removed.", Toast.LENGTH_SHORT)
                .show();

        // Return to the event list
        var action = AdminEventDetailsFragmentDirections.actionEventDetailsToNavHome();
        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(action);
    }

    /**
     * The dialog closed with a negative click (cancel).
     * Do nothing.
     * @param dialog The dialog that was showed.
     */
    @Override
    public void onDialogCancelClick(DialogFragment dialog) {}
}
