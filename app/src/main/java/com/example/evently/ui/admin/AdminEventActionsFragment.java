package com.example.evently.ui.admin;

import java.time.Instant;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.evently.databinding.FragmentAdminEventActionsBinding;
import com.example.evently.ui.model.EventViewModel;
import com.example.evently.ui.organizer.EditEventDetailsFragment;

/**
 * The actions button for the admin {@link EditEventDetailsFragment } fragment.
 * This takes care of setting up the action buttons for the admin view.
 * MUST be used by {@link EditEventDetailsFragment } as it requires ViewModels from there.
 */
public class AdminEventActionsFragment extends Fragment {
	private FragmentAdminEventActionsBinding binding;

	private EventViewModel eventViewModel;

	@Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding =
				FragmentAdminEventActionsBinding.inflate(getLayoutInflater(), container, false);

		eventViewModel = new ViewModelProvider(requireParentFragment()).get(EventViewModel.class);

		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// TODO Implement viewing the organizer and removing the event
		/*
		eventViewModel.getEventLive().observe(getViewLifecycleOwner(), event -> {
			if (event.selectionTime().toInstant().isBefore(Instant.now())) {
				binding.notifySelected.setEnabled(true);
				binding.notifySelected.setOnClickListener(v -> {
					binding.notifySelected.setEnabled(false);
					new NotificationDB().storeNotification(winnerNotification(event.eventID()));
				});
			} else {
				binding.notifySelected.setEnabled(false);
			}
		});

		binding.utilShareBtn.shareBtn.setOnClickListener(v -> {
			final var qrDialog = new EventQRDialogFragment();
			final var bundle = new Bundle();
			bundle.putSerializable("eventID", eventViewModel.eventID);
			qrDialog.setArguments(bundle);
			qrDialog.show(getChildFragmentManager(), "QR_DIALOG");
		});

		 */
	}
}
