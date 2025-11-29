package com.example.evently.ui.organizer;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.evently.data.model.EventEntrants;
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
    private EventEntrants entrantsDB;
    private static final int CREATE_FILE = 1;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        binding =
                FragmentOrganizerEventActionsBinding.inflate(getLayoutInflater(), container, false);

        eventViewModel = new ViewModelProvider(requireParentFragment()).get(EventViewModel.class);

        entrantsDB = new EventEntrants(eventViewModel.eventID);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.utilShareBtn.shareBtn.setOnClickListener(v -> {
            final var qrDialog = new EventQRDialogFragment();
            final var bundle = new Bundle();
            bundle.putSerializable("eventID", eventViewModel.eventID);
            qrDialog.setArguments(bundle);
            qrDialog.show(getChildFragmentManager(), "QR_DIALOG");
        });

        binding.sendNotif.setText(String.format("Notify %s", currentlySelectedChannel));
        binding.selectChannel.setCheckable(true);
        binding.selectChannel.setOnClickListener(this::selectChannel);
        binding.sendNotif.setOnClickListener(v -> NavHostFragment.findNavController(this)
                .navigate(EditEventDetailsFragmentDirections.actionEventDetailsToNavThread(
                        eventViewModel.eventID, currentlySelectedChannel)));

        binding.exportEntrants.setClickable(!entrantsDB.accepted().isEmpty());
        binding.exportEntrants.setOnClickListener(this::exportCSV);
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
        var popup = new PopupMenu(getContext(), v);
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

    private void exportCSV(View v) {
        if (entrantsDB.accepted().isEmpty()) return;

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/csv");
        intent.putExtra(Intent.EXTRA_TITLE, "entrants.csv");

        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() != OrganizerActivity.RESULT_OK)
                        return; // May need to change returns to making a toast

                    if (result.getData() == null) return;
                    var uri = result.getData().getData();
                    if (uri == null) return;

                    var csvString = String.join(",", entrantsDB.accepted());

                    try (OutputStream os =
                            requireContext().getContentResolver().openOutputStream(uri)) {
                        os.write(csvString.getBytes(StandardCharsets.UTF_8));
                        os.flush();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .launch(intent);
    }
}
