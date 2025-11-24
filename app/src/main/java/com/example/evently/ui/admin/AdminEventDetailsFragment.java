package com.example.evently.ui.admin;

import androidx.fragment.app.DialogFragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.evently.data.EventsDB;
import com.example.evently.ui.common.ConfirmDeleteDialog;
import com.example.evently.ui.common.EventDetailsFragment;
import com.example.evently.ui.organizer.EventPeopleFragment;

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

    @Override
    public void onDialogConfirmClick(DialogFragment dialog) {
        // Delete event
        final EventsDB eventDB = new EventsDB();
        eventDB.deleteEvent(eventViewModel.eventID);

        // Return to the event list
        var action = AdminEventDetailsFragmentDirections.actionEventDetailsToNavHome();
        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(action);
    }

    @Override
    public void onDialogCancelClick(DialogFragment dialog) {}
}
