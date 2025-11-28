package com.example.evently.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.evently.data.EventsDB;
import com.example.evently.databinding.FragmentAdminEventActionsBinding;
import com.example.evently.ui.common.ConfirmFragmentNoInput;
import com.example.evently.ui.model.EventViewModel;

/**
 * The actions button for the admin {@link AdminEventDetailsFragment } fragment.
 * This takes care of setting up the action buttons for the admin view.
 * Used by {@link AdminEventDetailsFragment } as it requires ViewModels from there.
 */
public class AdminEventActionsFragment extends Fragment {
    private FragmentAdminEventActionsBinding binding;

    private EventViewModel eventViewModel;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminEventActionsBinding.inflate(getLayoutInflater(), container, false);

        eventViewModel = new ViewModelProvider(requireParentFragment()).get(EventViewModel.class);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.removeEvent.setOnClickListener(v -> {
            ConfirmFragmentNoInput confirmFragment = ConfirmFragmentNoInput.newInstance(
                    "Delete Event",
                    "Are you sure you want to delete "
                            + eventViewModel.getEventLive().getValue().name());
            confirmFragment.show(getParentFragmentManager(), "confirmNoInput");
            getParentFragmentManager()
                    .setFragmentResultListener(
                            ConfirmFragmentNoInput.requestKey, this, this::onDialogConfirmClick);
        });
    }

    /**
     * The dialog closed with a confirm click.
     * Delete the event from the eventsDB and navigate back to the event list
     * @param requestKey key of request in bundle
     * @param result confirmation result
     */
    public void onDialogConfirmClick(String requestKey, Bundle result) {
        if (!result.getBoolean(ConfirmFragmentNoInput.inputKey)) return;
        // Delete event
        final EventsDB eventDB = new EventsDB();
        eventDB.deleteEvent(eventViewModel.eventID);

        Toast.makeText(requireContext(), "Event was removed.", Toast.LENGTH_SHORT)
                .show();

        // Return to the event list
        NavController navController = NavHostFragment.findNavController(this);
        navController.popBackStack();
    }
}
