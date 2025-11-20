package com.example.evently.ui.admin;

import com.example.evently.ui.common.EventDetailsFragment;
import com.example.evently.ui.admin.AdminEventActionsFragment;
import com.example.evently.ui.organizer.EventPeopleFragment;

public class AdminEventDetailsFragment extends
		EventDetailsFragment<EventPeopleFragment, AdminEventActionsFragment> {

	@Override
	protected Class<EventPeopleFragment> getFragmentForEntrantListContainer() {
		return EventPeopleFragment.class;
	}

	@Override
	protected Class<AdminEventActionsFragment> getFragmentForActionButtonsContainer() {
		return AdminEventActionsFragment.class;
	}
}
