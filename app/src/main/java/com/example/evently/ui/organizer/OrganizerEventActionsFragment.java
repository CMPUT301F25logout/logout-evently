package com.example.evently.ui.organizer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.evently.data.model.Notification;
import com.example.evently.databinding.FragmentOrganizerEventActionsBinding;
import com.example.evently.ui.common.EventQRDialogFragment;
import com.example.evently.ui.model.EventViewModel;

/**
 * The actions button for the organizer {@link EditEventDetailsFragment } fragment.
 * This takes care of setting up the action buttons for the organizer view.
 * MUST be used by {@link EditEventDetailsFragment } as it requires ViewModels from there.
 */
public class OrganizerEventActionsFragment extends Fragment {
    private FragmentOrganizerEventActionsBinding binding;
    private String currentlySelectedChannel = Notification.Channel.All.name();
    private EventViewModel eventViewModel;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        binding =
                FragmentOrganizerEventActionsBinding.inflate(getLayoutInflater(), container, false);

        eventViewModel = new ViewModelProvider(requireParentFragment()).get(EventViewModel.class);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        eventViewModel.getEventLive().observe(getViewLifecycleOwner(), ev -> {
            if (ev.requiresLocation()) {
                binding.openMap.setVisibility(View.VISIBLE);
            }
        });

        binding.utilShareBtn.shareBtn.setOnClickListener(v -> {
            final var qrDialog = new EventQRDialogFragment();
            final var bundle = new Bundle();
            bundle.putSerializable("eventID", eventViewModel.eventID);
            qrDialog.setArguments(bundle);
            qrDialog.show(getChildFragmentManager(), "QR_DIALOG");
        });

        binding.openMap.setOnClickListener(v ->
            new EventEntrantsMapFragment().show(getParentFragmentManager(), "map_dialog")
        );

        binding.sendNotif.setText(String.format("Notify %s", currentlySelectedChannel));
        binding.selectChannel.setCheckable(true);
        binding.selectChannel.setOnClickListener(this::selectChannel);
        binding.sendNotif.setOnClickListener(v -> NavHostFragment.findNavController(this)
                .navigate(EditEventDetailsFragmentDirections.actionEventDetailsToNavThread(
                        eventViewModel.eventID, currentlySelectedChannel)));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    /**
     * Shows the menu to select notification channel
     * @param v View of button
     */
    private void selectChannel(View v) {
        PopupMenu popup = new PopupMenu(getContext(), v);
        binding.selectChannel.setChecked(true);

        for (Notification.Channel channel : Notification.Channel.values())
            popup.getMenu().add(0, channel.ordinal(), channel.ordinal(), channel.name());

        popup.setOnMenuItemClickListener(menuItem -> {
            currentlySelectedChannel = (String) menuItem.getTitle();
            binding.sendNotif.setText(String.format("Notify %s", currentlySelectedChannel));
            return true;
        });

        popup.setOnDismissListener(menu -> binding.selectChannel.setChecked(false));
        popup.show();
    }
}
