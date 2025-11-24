package com.example.evently.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.evently.databinding.FragmentAdminEventActionsBinding;
import com.example.evently.ui.common.ConfirmDeleteDialog;
import com.example.evently.ui.model.EventViewModel;

/**
 * The actions button for the admin {@link AdminEventDetailsFragment } fragment.
 * This takes care of setting up the action buttons for the admin view.
 * MUST be used by {@link AdminEventDetailsFragment } as it requires ViewModels from there.
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
        /*
        binding.viewOrganizer.setOnClickListener(v -> {
            var action =
                    AdminEventDetailsFragmentDirections.actionEventDetailsToProfileDetails(
                            eventViewModel.getEventLive().getValue().organizer());
            NavController navController = NavHostFragment.findNavController(this);
            navController.navigate(action);
        });
        */

        binding.removeEvent.setOnClickListener(v -> {
            final ConfirmDeleteDialog cdDialog = new ConfirmDeleteDialog();

            // Make a bundle to store the arguments
            String title = "Delete Event";
            String message = "Are you sure you want to delete "
                    + eventViewModel.getEventLive().getValue().name();
            Bundle args = new Bundle();
            args.putString("title", title);
            args.putString("message", message);
            cdDialog.setArguments(args);
            cdDialog.show(getParentFragmentManager(), "ConfirmDeleteEventDialog");
        });
    }
}
