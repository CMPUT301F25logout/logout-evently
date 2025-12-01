package com.example.evently.ui.organizer;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.evently.data.EventsDB;
import com.example.evently.data.model.Notification;
import com.example.evently.databinding.FragmentOrganizerEventActionsBinding;
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
    private ActivityResultLauncher<Intent> createFile;
    private List<String> accepted;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        binding =
                FragmentOrganizerEventActionsBinding.inflate(getLayoutInflater(), container, false);

        eventViewModel = new ViewModelProvider(requireParentFragment()).get(EventViewModel.class);

        createFile = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    Log.i("CSV", "CSV write launched");
                    if (result.getResultCode() != OrganizerActivity.RESULT_OK) {
                        Log.w("CSV", "Result bad. code: " + result.getResultCode());
                        return; // May need to change returns to making a toast
                    }

                    if (result.getData() == null) return;
                    var uri = result.getData().getData();
                    if (uri == null) {
                        Log.w("CSV", "URI was null");
                        return;
                    }
                    try (OutputStream os =
                            requireContext().getContentResolver().openOutputStream(uri)) {
                        os.write(String.join(",", accepted).getBytes(StandardCharsets.UTF_8));
                        os.flush();
                        Log.i("CSV", "Exported CSV Successfully");
                    } catch (IOException e) {
                        Log.e("CSV", "CSV write error", e);
                    }
                });

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

        binding.openMap.setOnClickListener(
                v -> new EventEntrantsMapFragment().show(getParentFragmentManager(), "map_dialog"));

        binding.sendNotif.setText(String.format("Notify %s", currentlySelectedChannel));
        binding.selectChannel.setCheckable(true);
        binding.selectChannel.setOnClickListener(this::selectChannel);
        binding.sendNotif.setOnClickListener(v -> NavHostFragment.findNavController(this)
                .navigate(EditEventDetailsFragmentDirections.actionEventDetailsToNavThread(
                        eventViewModel.eventID, currentlySelectedChannel)));

        binding.exportEntrants.setEnabled(false);
        var eventsDB = new EventsDB();
        eventsDB.fetchEventEntrants(eventViewModel.eventID).optionally(entrants -> {
            accepted = entrants.accepted();
            if (accepted.isEmpty()) return;
            binding.exportEntrants.setEnabled(true);
            binding.exportEntrants.setOnClickListener(this::exportCSV);
        });
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

    /**
     * Exports a CSV of final participants to download
     * @param view view of button pressed
     */
    private void exportCSV(View view) {
        Log.i("CSV", "CSV write called");
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/csv");
        intent.putExtra(Intent.EXTRA_TITLE, "entrants.csv");
        createFile.launch(intent);
    }
}
