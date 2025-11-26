package com.example.evently.ui.organizer;

import static com.example.evently.data.model.Notification.winnerNotification;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.evently.data.NotificationDB;
import com.example.evently.data.model.Notification;
import com.example.evently.databinding.FragmentOrganizerEventActionsBinding;
import com.example.evently.databinding.FragmentOrganizerEventActionsMenuBinding;
import com.example.evently.ui.common.EventQRDialogFragment;
import com.example.evently.ui.entrant.EntrantActivity;
import com.example.evently.ui.model.EventViewModel;

/**
 * The actions button for the organizer {@link EditEventDetailsFragment } fragment.
 * This takes care of setting up the action buttons for the organizer view.
 * MUST be used by {@link EditEventDetailsFragment } as it requires ViewModels from there.
 */
public class OrganizerEventActionsFragment extends Fragment
        implements AdapterView.OnItemSelectedListener {
    private FragmentOrganizerEventActionsMenuBinding binding;
    private Notification.Channel currentlySelectedChannel = Notification.Channel.All;
    private EventViewModel eventViewModel;
    private final List<Notification.Channel> channels = Arrays.asList(Notification.Channel.values());

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding =
                FragmentOrganizerEventActionsMenuBinding.inflate(getLayoutInflater(), container, false);

        eventViewModel = new ViewModelProvider(requireParentFragment()).get(EventViewModel.class);

        final var channels = Notification.Channel.values();

        ArrayAdapter<Notification.Channel> adapter = new ArrayAdapter<>(
                this.getContext(),
                android.R.layout.simple_spinner_item,
                channels
        );

        binding.notifChannelSpinner.setAdapter(adapter);
        binding.notifChannelSpinner.setOnItemSelectedListener(this);
        binding.notifChannelSpinner.setSelection(0);
        //TODO: Move out of material button layout, make a linearlayout with spinner and buttons
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.materialButtons.utilShareBtn.shareBtn.setOnClickListener(v -> {
            final var qrDialog = new EventQRDialogFragment();
            final var bundle = new Bundle();
            bundle.putSerializable("eventID", eventViewModel.eventID);
            qrDialog.setArguments(bundle);
            qrDialog.show(getChildFragmentManager(), "QR_DIALOG");
        });

        binding.materialButtons.sendNotification.setOnClickListener(this::sendNotification);
    }

    private void sendNotification(View view) {
        final var bundle = new Bundle();
        bundle.putSerializable("eventID", eventViewModel.eventID);
        bundle.putSerializable("channel", currentlySelectedChannel);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (currentlySelectedChannel.ordinal() == position) return;
        currentlySelectedChannel = channels.get(position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {}
}
